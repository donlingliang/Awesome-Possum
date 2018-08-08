package codelabs.don.codelabs.room.prepopulate

import android.util.Log

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Created by don on 8/7/18
 */
class RoomSQLiteUtils {

    companion object {
        private val TAG = CustomSQLiteHelper::class.simpleName

        fun splitSqlScript(script: String, delim: Char): List<String> {
            val statements = arrayListOf<String>()
            var sb = StringBuilder()
            var inLiteral = false
            val content = script.toCharArray()

            for (i in 0 until script.length) {
                if (content[i] == '"') {
                    inLiteral = !inLiteral
                }

                if (content[i] == delim && !inLiteral) {
                    if (sb.length > 0) {
                        statements.add(sb.toString().trim { it <= ' ' })
                        sb = StringBuilder()
                    }
                } else {
                    sb.append(content[i])
                }
            }

            if (sb.length > 0) {
                statements.add(sb.toString().trim { it <= ' ' })
            }

            return statements
        }

        @Throws(IOException::class)
        fun writeExtractedFileToDisk(`in`: InputStream, outs: OutputStream) {
            val buffer = ByteArray(1024)

            var length: Int = `in`.read(buffer)
            while (length > 0) {
                outs.write(buffer, 0, length)
                length = `in`.read(buffer)
            }

            outs.flush()
            outs.close()
            `in`.close()
        }

        @Throws(IOException::class)
        fun getFileFromZip(zipFileStream: InputStream): ZipInputStream? {
            val zis = ZipInputStream(zipFileStream)
            val ze: ZipEntry = zis.nextEntry
            if (ze != null) {
                Log.w(TAG, "extracting file: '" + ze.name + "'...")
                return zis
            } else {
                return null
            }
        }

        fun convertStreamToString(`is`: InputStream): String {
            return Scanner(`is`).useDelimiter("\\A").next()
        }
    }

}