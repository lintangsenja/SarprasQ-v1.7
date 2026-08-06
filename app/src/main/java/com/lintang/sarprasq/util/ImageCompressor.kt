package com.lintang.sarprasq.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt

data class CompressedImageResult(
    val bytes: ByteArray,
    val originalWidth: Int,
    val originalHeight: Int,
    val finalWidth: Int,
    val finalHeight: Int,
    val sizeInBytes: Long,
    val sizeInKb: Double,
    val isAspectPreserved: Boolean = true,
    val isProfilePhoto: Boolean = false,
    val compressionApplied: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CompressedImageResult
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}

object ImageCompressor {

    private const val TAG = "ImageCompressor"
    const val MAX_PROOF_SIZE_BYTES = 400 * 1024 // 400 KB untuk foto bukti
    const val MAX_PROFILE_SIZE_BYTES = 5 * 1024 * 1024 // 5 MB untuk foto profil

    /**
     * Memproses foto bukti (max 400 KB, aspect ratio & EXIF rotasi terjaga)
     * lalu mengunggah ke Firebase Storage untuk menghasilkan URL publik yang dapat diakses seluruh pengguna.
     */
    suspend fun processAndUploadPhoto(
        context: Context,
        imageUri: Uri,
        folder: String = "bukti_kerusakan",
        onStatusUpdate: (String) -> Unit = {}
    ): Pair<String, CompressedImageResult?> = withContext(Dispatchers.IO) {
        onStatusUpdate("Mengompresi foto bukti (max 400 KB) & membaca EXIF...")
        val compressed = compressImage(context, imageUri, maxSizeBytes = MAX_PROOF_SIZE_BYTES)
        if (compressed == null) {
            onStatusUpdate("Format gambar tidak dapat dibaca.")
            return@withContext Pair(imageUri.toString(), null)
        }

        val sizeKbStr = String.format(Locale.US, "%.1f", compressed.sizeInKb)
        onStatusUpdate("Foto dikompres ($sizeKbStr KB). Mengunggah ke Firebase Storage...")

        try {
            val timestamp = System.currentTimeMillis()
            val fileName = "bukti_${timestamp}.jpg"
            val storageInstance = FirebaseStorage.getInstance()
            val storageRef = storageInstance.reference.child("$folder/$fileName")

            val uploadTask = storageRef.putBytes(compressed.bytes)

            val downloadUrl = suspendCancellableCoroutine<String> { continuation ->
                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        if (continuation.isActive) continuation.resume(uri.toString())
                    }.addOnFailureListener { ex ->
                        if (continuation.isActive) continuation.resumeWithException(ex)
                    }
                }.addOnFailureListener { ex ->
                    if (continuation.isActive) continuation.resumeWithException(ex)
                }
            }

            Log.i(TAG, "Foto bukti terunggah ke Firebase Storage (URL Publik): $downloadUrl ($sizeKbStr KB)")
            onStatusUpdate("✓ Foto bukti dikompres ($sizeKbStr KB) & diunggah ke Firebase Storage (Publik)")
            Pair(downloadUrl, compressed)
        } catch (e: Exception) {
            Log.e(TAG, "Storage Upload Warning / Offline: ${e.message}")
            onStatusUpdate("⚠ Dikompres ($sizeKbStr KB). Storage Offline/Unconfigured. Menggunakan URI lokal.")
            Pair(imageUri.toString(), compressed)
        }
    }

    /**
     * Memproses foto profil:
     * - Jika file <= 5 MB: Dibiarkan original (tidak dikompres), kualitas 100% asli dipertahankan agar detail wajah tetap tajam.
     * - Jika file > 5 MB: Kompresi adaptif aman sampai ukuran <= 5 MB.
     * Mengunggah ke Firebase Storage folder "foto_profil" dan mengembalikan URL publik.
     */
    suspend fun processAndUploadProfilePhoto(
        context: Context,
        imageUri: Uri,
        onStatusUpdate: (String) -> Unit = {}
    ): Pair<String, CompressedImageResult?> = withContext(Dispatchers.IO) {
        onStatusUpdate("Memeriksa ukuran foto profil...")
        val profileResult = processProfileImage(context, imageUri)
        if (profileResult == null) {
            onStatusUpdate("Gagal membaca foto profil.")
            return@withContext Pair(imageUri.toString(), null)
        }

        val sizeMbStr = String.format(Locale.US, "%.2f", profileResult.sizeInBytes / (1024.0 * 1024.0))
        if (!profileResult.compressionApplied) {
            onStatusUpdate("Foto profil <= 5 MB ($sizeMbStr MB). Kualitas asli dipertahankan. Mengunggah ke Firebase Storage...")
        } else {
            onStatusUpdate("Foto profil > 5 MB dikompresi adaptif ke $sizeMbStr MB. Mengunggah ke Firebase Storage...")
        }

        try {
            val timestamp = System.currentTimeMillis()
            val fileName = "profil_${timestamp}.jpg"
            val storageInstance = FirebaseStorage.getInstance()
            val storageRef = storageInstance.reference.child("foto_profil/$fileName")

            val uploadTask = storageRef.putBytes(profileResult.bytes)

            val downloadUrl = suspendCancellableCoroutine<String> { continuation ->
                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        if (continuation.isActive) continuation.resume(uri.toString())
                    }.addOnFailureListener { ex ->
                        if (continuation.isActive) continuation.resumeWithException(ex)
                    }
                }.addOnFailureListener { ex ->
                    if (continuation.isActive) continuation.resumeWithException(ex)
                }
            }

            Log.i(TAG, "Foto profil terunggah ke Firebase Storage: $downloadUrl ($sizeMbStr MB)")
            onStatusUpdate("✓ Foto profil terunggah ($sizeMbStr MB). URL publik aktif.")
            Pair(downloadUrl, profileResult)
        } catch (e: Exception) {
            Log.e(TAG, "Profile Storage Upload Offline / Exception: ${e.message}")
            onStatusUpdate("⚠ Foto profil ($sizeMbStr MB). Storage Offline. Menyimpan URL lokal.")
            Pair(imageUri.toString(), profileResult)
        }
    }

    /**
     * Memproses bitmap foto profil (misalnya dari hasil crop manual)
     * - Jika size <= 5 MB: tanpa kompresi (quality 100%).
     * - Jika size > 5 MB: kompresi adaptif hingga <= 5 MB.
     */
    suspend fun processAndUploadProfileBitmap(
        context: Context,
        bitmap: Bitmap,
        onStatusUpdate: (String) -> Unit = {}
    ): Pair<String, CompressedImageResult?> = withContext(Dispatchers.IO) {
        onStatusUpdate("Memeriksa ukuran bitmap foto profil...")

        var baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        var bytes = baos.toByteArray()
        var compressionApplied = false

        if (bytes.size > MAX_PROFILE_SIZE_BYTES) {
            compressionApplied = true
            var quality = 95
            baos.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            bytes = baos.toByteArray()

            while (bytes.size > MAX_PROFILE_SIZE_BYTES && quality > 30) {
                baos.reset()
                quality -= 10
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                bytes = baos.toByteArray()
            }
        }

        val sizeMb = bytes.size / (1024.0 * 1024.0)
        val sizeMbStr = String.format(Locale.US, "%.2f", sizeMb)
        val statusText = if (!compressionApplied) "Foto profil <= 5 MB ($sizeMbStr MB, kualitas asli)." else "Foto profil dikompres ke $sizeMbStr MB."
        onStatusUpdate("$statusText Mengunggah ke Firebase Storage...")

        val profileResult = CompressedImageResult(
            bytes = bytes,
            originalWidth = bitmap.width,
            originalHeight = bitmap.height,
            finalWidth = bitmap.width,
            finalHeight = bitmap.height,
            sizeInBytes = bytes.size.toLong(),
            sizeInKb = bytes.size / 1024.0,
            isAspectPreserved = true,
            isProfilePhoto = true,
            compressionApplied = compressionApplied
        )

        try {
            val timestamp = System.currentTimeMillis()
            val fileName = "profil_${timestamp}.jpg"
            val storageInstance = FirebaseStorage.getInstance()
            val storageRef = storageInstance.reference.child("foto_profil/$fileName")

            val uploadTask = storageRef.putBytes(bytes)

            val downloadUrl = suspendCancellableCoroutine<String> { continuation ->
                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        if (continuation.isActive) continuation.resume(uri.toString())
                    }.addOnFailureListener { ex ->
                        if (continuation.isActive) continuation.resumeWithException(ex)
                    }
                }.addOnFailureListener { ex ->
                    if (continuation.isActive) continuation.resumeWithException(ex)
                }
            }

            Log.i(TAG, "Bitmap foto profil terunggah: $downloadUrl ($sizeMbStr MB)")
            onStatusUpdate("✓ Foto profil terunggah ($sizeMbStr MB).")
            Pair(downloadUrl, profileResult)
        } catch (e: Exception) {
            Log.e(TAG, "Profile Bitmap Storage Upload Error: ${e.message}")
            onStatusUpdate("⚠ Foto profil ($sizeMbStr MB). Storage offline/unconfigured.")
            Pair("", profileResult)
        }
    }

    private fun processProfileImage(context: Context, imageUri: Uri): CompressedImageResult? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri) ?: return null
            val rawBytes = inputStream.use { it.readBytes() }
            val originalSize = rawBytes.size

            val orientation = getExifOrientation(context, imageUri)

            // Jika ukuran <= 5 MB dan tidak ada rotasi EXIF khusus, gunakan bytes ASLI 100% tanpa kompresi
            if (originalSize <= MAX_PROFILE_SIZE_BYTES && orientation == ExifInterface.ORIENTATION_NORMAL) {
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, boundsOptions)

                return CompressedImageResult(
                    bytes = rawBytes,
                    originalWidth = boundsOptions.outWidth,
                    originalHeight = boundsOptions.outHeight,
                    finalWidth = boundsOptions.outWidth,
                    finalHeight = boundsOptions.outHeight,
                    sizeInBytes = rawBytes.size.toLong(),
                    sizeInKb = rawBytes.size / 1024.0,
                    isAspectPreserved = true,
                    isProfilePhoto = true,
                    compressionApplied = false // TANPA KOMPRESI
                )
            }

            // Jika ada rotasi EXIF atau ukuran > 5 MB
            val decodeOptions = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            var loadedBitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions) ?: return null
            loadedBitmap = rotateBitmap(loadedBitmap, orientation)

            var currentBitmap = loadedBitmap
            var baos = ByteArrayOutputStream()

            if (originalSize <= MAX_PROFILE_SIZE_BYTES) {
                // Hanya perbaiki rotasi EXIF, simpan dengan kualitas 100%
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val bytes = baos.toByteArray()
                return CompressedImageResult(
                    bytes = bytes,
                    originalWidth = loadedBitmap.width,
                    originalHeight = loadedBitmap.height,
                    finalWidth = currentBitmap.width,
                    finalHeight = currentBitmap.height,
                    sizeInBytes = bytes.size.toLong(),
                    sizeInKb = bytes.size / 1024.0,
                    isAspectPreserved = true,
                    isProfilePhoto = true,
                    compressionApplied = false // TANPA KOMPRESI KUALITAS
                )
            }

            // Jika ukuran > 5 MB, kompresi adaptif hingga <= 5 MB
            var quality = 95
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)

            while (baos.toByteArray().size > MAX_PROFILE_SIZE_BYTES && quality > 30) {
                baos.reset()
                quality -= 10
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            }

            val currentAspectRatio = currentBitmap.width.toFloat() / currentBitmap.height.toFloat()
            var scaleFactor = 0.9f
            while (baos.toByteArray().size > MAX_PROFILE_SIZE_BYTES && currentBitmap.width > 500) {
                baos.reset()
                val targetW = (currentBitmap.width * scaleFactor).roundToInt()
                val targetH = (targetW / currentAspectRatio).roundToInt()

                val scaledBitmap = Bitmap.createScaledBitmap(currentBitmap, targetW, targetH, true)
                if (scaledBitmap != currentBitmap && !currentBitmap.isRecycled) {
                    currentBitmap.recycle()
                }
                currentBitmap = scaledBitmap
                quality = 85
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                scaleFactor -= 0.1f
            }

            val resultBytes = baos.toByteArray()
            CompressedImageResult(
                bytes = resultBytes,
                originalWidth = loadedBitmap.width,
                originalHeight = loadedBitmap.height,
                finalWidth = currentBitmap.width,
                finalHeight = currentBitmap.height,
                sizeInBytes = resultBytes.size.toLong(),
                sizeInKb = resultBytes.size / 1024.0,
                isAspectPreserved = true,
                isProfilePhoto = true,
                compressionApplied = true // Dikompresi karena > 5 MB
            )
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memproses foto profil: ${e.message}", e)
            null
        }
    }

    /**
     * Kompresi foto bukti dari Uri menjadi ByteArray dengan ukuran mentok <= 400 KB.
     * Menjaga rasio aspek asli (aspect ratio) dan menangani rotasi EXIF.
     */
    fun compressImage(
        context: Context,
        imageUri: Uri,
        maxSizeBytes: Int = MAX_PROOF_SIZE_BYTES
    ): CompressedImageResult? {
        return try {
            val contentResolver = context.contentResolver

            // 1. Baca Orientasi EXIF
            val orientation = getExifOrientation(context, imageUri)

            // 2. Baca dimensi awal gambar tanpa memuat penuh ke memori
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it, null, boundsOptions)
            }

            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight

            if (origWidth <= 0 || origHeight <= 0) {
                Log.e(TAG, "Gagal membaca dimensi gambar dari Uri: $imageUri")
                return null
            }

            val maxInitialDim = 1920
            val sampleSize = calculateInSampleSize(origWidth, origHeight, maxInitialDim, maxInitialDim)

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            var loadedBitmap = contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return null

            // 3. Putar bitmap sesuai orientasi EXIF
            loadedBitmap = rotateBitmap(loadedBitmap, orientation)

            var currentBitmap = loadedBitmap
            val currentAspectRatio = currentBitmap.width.toFloat() / currentBitmap.height.toFloat()

            // 4. Kompresi ke format JPEG secara bertahap (kualitas 85 -> 25)
            var quality = 85
            var baos = ByteArrayOutputStream()
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)

            while (baos.toByteArray().size > maxSizeBytes && quality > 25) {
                baos.reset()
                quality -= 10
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            }

            // 5. Jika masih > 400 KB, ubah dimensi secara proporsional (jaga rasio aspek)
            var scaleFactor = 0.85f
            while (baos.toByteArray().size > maxSizeBytes && currentBitmap.width > 320 && currentBitmap.height > 320) {
                baos.reset()
                val targetW = (currentBitmap.width * scaleFactor).roundToInt().coerceAtLeast(200)
                val targetH = (targetW / currentAspectRatio).roundToInt().coerceAtLeast(200)

                val scaledBitmap = Bitmap.createScaledBitmap(currentBitmap, targetW, targetH, true)
                if (scaledBitmap != currentBitmap && !currentBitmap.isRecycled) {
                    currentBitmap.recycle()
                }
                currentBitmap = scaledBitmap
                quality = 75
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                scaleFactor -= 0.08f
            }

            val resultBytes = baos.toByteArray()
            val resultSizeKb = resultBytes.size / 1024.0

            Log.i(TAG, "Kompresi bukti sukses: ${resultBytes.size} bytes (${String.format(Locale.US, "%.1f", resultSizeKb)} KB). Dimensi: ${currentBitmap.width}x${currentBitmap.height}, Rasio aspek terjaga.")

            CompressedImageResult(
                bytes = resultBytes,
                originalWidth = origWidth,
                originalHeight = origHeight,
                finalWidth = currentBitmap.width,
                finalHeight = currentBitmap.height,
                sizeInBytes = resultBytes.size.toLong(),
                sizeInKb = resultSizeKb,
                isAspectPreserved = true,
                isProfilePhoto = false,
                compressionApplied = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengompres gambar: ${e.message}", e)
            null
        }
    }

    private fun getExifOrientation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val exif = ExifInterface(inputStream)
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } else {
                    ExifInterface.ORIENTATION_NORMAL
                }
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            Log.w(TAG, "Gagal membaca data EXIF: ${e.message}")
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }

        return try {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            rotated
        } catch (e: Exception) {
            Log.e(TAG, "Gagal rotasi bitmap EXIF: ${e.message}")
            bitmap
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
