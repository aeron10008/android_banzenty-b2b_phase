package com.restart.banzenty.utils

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import androidx.core.content.ContextCompat
import com.restart.banzenty.R
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainUtils {

    companion object {
        fun encodeImage(bm: Bitmap?): String? {
            val baos = ByteArrayOutputStream()
            bm?.compress(Bitmap.CompressFormat.JPEG, 40, baos)
            val b = baos.toByteArray()
            return Base64.encodeToString(b, Base64.DEFAULT)
        }


        fun initializeSelectedLanguage(context: Context, userLanguage: String?): Configuration {
            // Create a new Locale object
            val locale = Locale(userLanguage, "MA")
            Locale.setDefault(locale)
            // Create a new configuration object
            val config = Configuration()
            // Set the locale of the new configuration
            config.locale = locale
            // Update the configuration of the App context
            context.resources.updateConfiguration(
                config,
                context.resources.displayMetrics
            )
            return config
        }

        fun isNetworkError(msg: String): Boolean {
            when {
                msg.contains(Constants.UNABLE_TO_RESOLVE_HOST) -> return true
                else -> return false
            }
        }

        fun isPaginationDone(msg: String): Boolean {
            when {
                msg.contains(Constants.PAGINATION_DONE) -> return true
                else -> return false
            }
        }

        fun getPath(context: Context, uri: Uri): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    return getDataColumn(
                        context,
                        contentUri,
                        selection,
                        selectionArgs
                    )
                } else if (isGoogleDrive(uri)) { // Google Drive
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(";").toTypedArray()
                    val acc = split[0]
                    val doc = split[1]

                    /*
                 * @details google drive document data. - acc , docId.
                 * */try {
                        return saveFileIntoExternalStorageByUri(context, uri)
                            .toString()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                return getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        fun isGoogleDrive(uri: Uri): Boolean {
            return uri.authority.equals("com.google.android.apps.docs.storage", ignoreCase = true)
        }

        @Throws(java.lang.Exception::class)
        fun saveFileIntoExternalStorageByUri(context: Context, uri: Uri): File? {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalSize = inputStream!!.available()
            var bis: BufferedInputStream? = null
            var bos: BufferedOutputStream? = null
            val fileName = getFileName(context, uri)
            val file = makeEmptyFileIntoExternalStorageWithTitle(fileName)
            bis = BufferedInputStream(inputStream)
            bos = BufferedOutputStream(
                FileOutputStream(
                    file, false
                )
            )
            val buf = ByteArray(originalSize)
            bis.read(buf)
            do {
                bos.write(buf)
            } while (bis.read(buf) != -1)
            bos.flush()
            bos.close()
            bis.close()
            return file
        }

        fun makeEmptyFileIntoExternalStorageWithTitle(title: String?): File {
            val root = Environment.getExternalStorageDirectory().absolutePath
            return File(root, title)
        }

        fun getFileName(context: Context, uri: Uri): String? {
            var result: String? = null
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                } finally {
                    cursor!!.close()
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result!!.lastIndexOf('/')
                if (cut != -1) {
                    result = result.substring(cut + 1)
                }
            }
            return result
        }

        /**
         * Get the value of the data column for this Uri. This is useful for
         * MediaStore Uris, and other file-based ContentProviders.
         *
         * @param context       The context.
         * @param uri           The Uri to query.
         * @param selection     (Optional) Filter used in the query.
         * @param selectionArgs (Optional) Selection arguments used in the query.
         * @return The value of the _data column, which is typically a file path.
         */
        fun getDataColumn(
            context: Context, uri: Uri?, selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = context.contentResolver.query(
                    uri!!, projection, selection, selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        fun getTimeOnly(serverDate: String?): String? {
            var serverDate = serverDate
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            try {
                val dateObj = sdf.parse(serverDate)
                serverDate =
                    SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(dateObj)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return serverDate
        }

        fun getDateTime(serverDate: String?): String? {
            var serverDate = serverDate
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            try {
                val dateObj = sdf.parse(serverDate)
                serverDate =
                    SimpleDateFormat("dd-MM-yyyy  hh:mm aa", Locale.getDefault()).format(dateObj)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return serverDate
        }

        fun getDateOnly(serverDate: String?): String? {
            var serverDate = serverDate
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
            try {
                val dateObj: Date = sdf.parse(serverDate)
                serverDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(dateObj)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return serverDate
        }

        fun openGoogleMap(
            context: Context,
            destinationLatitude: Double,
            destinationLongitude: Double
        ) {
            val uri =
                "http://maps.google.com/maps?daddr=$destinationLatitude,$destinationLongitude"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.setPackage("com.google.android.apps.maps")
            context.startActivity(intent)
        }

        fun getCustomizedMarker(context: Context,drawable:Int): Bitmap? {
            val height = 150
            val width = 150
            val bitmapDraw = ContextCompat.getDrawable(
                context,
                drawable
            ) as BitmapDrawable?
            val b = bitmapDraw!!.bitmap
            return Bitmap.createScaledBitmap(b, width, height, false)
        }


        /*
****Check Language*****
 */
        fun checkIfTextIsArabic(carPlateCharacters: String): Boolean {
            var i = 0
            while (i < carPlateCharacters.length) {
                val char: Int = carPlateCharacters.codePointAt(i)

                if (char in 0x0600..0x06E0) {
                    return true
                }

                i += Character.charCount(char)
            }

            return false
        }
    }
}