package com.example.teams_app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class WhatsAppHelper {
    companion object {
        // Package names for different WhatsApp versions
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"

        /**
         * Send a WhatsApp message to the specified phone number with minimal UI interaction
         * Note: WhatsApp requires user interaction for security reasons
         *
         * @param context Context for launching intent and showing toast
         * @param phoneNumber The phone number to send message to (with country code)
         * @param message The message to send
         * @return Boolean indicating whether the attempt was successful
         */
        fun sendWhatsAppMessage(context: Context, phoneNumber: String, message: String): Boolean {
            try {
                // Format the phone number properly
                val formattedNumber = formatPhoneNumber(phoneNumber)

                // Use the most direct method available with WhatsApp
                val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())

                // Use the click-to-chat API which is the most direct method
                val uri = Uri.parse(
                    "https://wa.me/${formattedNumber.replace("+", "")}?text=$encodedMessage"
                )

                val intent = Intent(Intent.ACTION_VIEW, uri)

                // Attempt to launch with minimal UI
                if (launchWhatsAppDirectly(context, intent)) {
                    // Log the message as sent
                    Toast.makeText(context, "Message prepared for sending", Toast.LENGTH_SHORT).show()
                    return true
                } else {
                    Toast.makeText(context, "WhatsApp not found or could not be launched", Toast.LENGTH_SHORT).show()
                    return false
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        /**
         * Launch WhatsApp in the most direct way possible
         *
         * @param context Context for launching intent
         * @param intent Intent configured for WhatsApp
         * @return Boolean indicating success or failure
         */
        private fun launchWhatsAppDirectly(context: Context, intent: Intent): Boolean {
            try {
                // Try different approaches in order of most direct to least direct

                // First try regular WhatsApp
                if (isPackageInstalled(context, WHATSAPP_PACKAGE)) {
                    intent.setPackage(WHATSAPP_PACKAGE)
                    context.startActivity(intent)
                    return true
                }

                // Then try WhatsApp Business
                if (isPackageInstalled(context, WHATSAPP_BUSINESS_PACKAGE)) {
                    intent.setPackage(WHATSAPP_BUSINESS_PACKAGE)
                    context.startActivity(intent)
                    return true
                }

                // If no WhatsApp app is installed, try browser
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                return false
            }
        }

        /**
         * Check if a specific package is installed
         *
         * @param context Context for accessing package manager
         * @param packageName Package name to check
         * @return Boolean indicating if package is installed
         */
        private fun isPackageInstalled(context: Context, packageName: String): Boolean {
            return try {
                val packageManager = context.packageManager
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Format phone number to ensure it has proper country code
         *
         * @param phoneNumber Raw phone number input
         * @return Properly formatted phone number with country code
         */
        private fun formatPhoneNumber(phoneNumber: String): String {
            // Remove spaces, dashes, etc.
            var formattedNumber = phoneNumber.replace("\\s+".toRegex(), "")
                .replace("-", "")

            // Make sure it has the + prefix for country code
            if (!formattedNumber.startsWith("+")) {
                // If no + prefix and starts with a 0, assume it needs the country code
                if (formattedNumber.startsWith("0")) {
                    formattedNumber = "+91" + formattedNumber.substring(1)
                } else if (!formattedNumber.startsWith("+91")) {
                    // If doesn't start with country code, add it
                    formattedNumber = "+91$formattedNumber"
                }
            }

            return formattedNumber
        }

        /**
         * Log that a WhatsApp message was sent
         *
         * @param context Context for accessing SharedPreferences
         * @param memberName Name of the member the message was sent to
         * @param phoneNumber Phone number the message was sent to
         * @param message Content of the message
         */
        fun logWhatsAppMessageSent(context: Context, memberName: String, phoneNumber: String, message: String) {
            val whatsAppPrefs = context.getSharedPreferences("WhatsAppLog", Context.MODE_PRIVATE)
            val timestamp = System.currentTimeMillis()
            val editor = whatsAppPrefs.edit()

            // Store the WhatsApp message details
            editor.putString("whatsapp_$timestamp", "$memberName|$phoneNumber|$message")
            editor.apply()
        }
    }
}