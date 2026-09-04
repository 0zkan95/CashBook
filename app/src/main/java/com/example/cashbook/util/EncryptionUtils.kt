package com.example.cashbook.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val ITERATION_COUNT = 1000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 16

    fun encrypt(data: ByteArray, password: CharArray): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(IV_LENGTH)
        random.nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

        val encryptedData = cipher.doFinal(data)
        
        // Return format: salt | iv | encryptedData
        val combined = ByteArray(SALT_LENGTH + IV_LENGTH + encryptedData.size)
        System.arraycopy(salt, 0, combined, 0, SALT_LENGTH)
        System.arraycopy(iv, 0, combined, SALT_LENGTH, IV_LENGTH)
        System.arraycopy(encryptedData, 0, combined, SALT_LENGTH + IV_LENGTH, encryptedData.size)
        
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedString: String, password: CharArray): ByteArray {
        val combined = Base64.decode(encryptedString, Base64.DEFAULT)
        
        val salt = ByteArray(SALT_LENGTH)
        System.arraycopy(combined, 0, salt, 0, SALT_LENGTH)
        
        val iv = ByteArray(IV_LENGTH)
        System.arraycopy(combined, SALT_LENGTH, iv, 0, IV_LENGTH)
        
        val encryptedData = ByteArray(combined.size - SALT_LENGTH - IV_LENGTH)
        System.arraycopy(combined, SALT_LENGTH + IV_LENGTH, encryptedData, 0, encryptedData.size)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

        return cipher.doFinal(encryptedData)
    }
}
