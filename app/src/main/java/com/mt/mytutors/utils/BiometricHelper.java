package com.mt.mytutors.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

/**
 * BiometricHelper - Clase utilitaria para autenticación biométrica
 *
 * Maneja la verificación de disponibilidad y la autenticación con huella digital
 */
public class BiometricHelper {

    /**
     * Callback para resultados de autenticación biométrica
     */
    public interface BiometricCallback {
        void onSuccess();
        void onError(String error);
        void onFailed();
    }

    /**
     * Verifica si la autenticación biométrica está disponible en el dispositivo
     *
     * @param context Contexto de la aplicación
     * @return true si la biometría está disponible y configurada
     */
    public static boolean isBiometricAvailable(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);

        int result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK |
                        BiometricManager.Authenticators.BIOMETRIC_STRONG
        );

        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Obtiene un mensaje descriptivo del estado de la biometría
     *
     * @param context Contexto de la aplicación
     * @return Mensaje describiendo el estado
     */
    public static String getBiometricStatus(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);

        int result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK |
                        BiometricManager.Authenticators.BIOMETRIC_STRONG
        );

        switch (result) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return "Biometría disponible";
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "Este dispositivo no tiene sensor biométrico";
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "Sensor biométrico no disponible temporalmente";
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "No hay huellas registradas. Configura una en Ajustes";
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                return "Se requiere actualización de seguridad";
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                return "Biometría no soportada";
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
            default:
                return "Estado de biometría desconocido";
        }
    }

    /**
     * Inicia el proceso de autenticación biométrica
     *
     * @param activity FragmentActivity desde donde se llama
     * @param callback Callback para manejar el resultado
     */
    public static void authenticate(FragmentActivity activity, BiometricCallback callback) {
        // Verificar disponibilidad primero
        if (!isBiometricAvailable(activity)) {
            callback.onError(getBiometricStatus(activity));
            return;
        }

        // Executor para el hilo principal
        Executor executor = ContextCompat.getMainExecutor(activity);

        // Crear el prompt de autenticación
        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        callback.onSuccess();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode,
                                                      @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);

                        // Ignorar si el usuario canceló
                        if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                                errorCode == BiometricPrompt.ERROR_CANCELED) {
                            return;
                        }

                        callback.onError(errString.toString());
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        callback.onFailed();
                    }
                });

        // Configurar la información del prompt
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación Biométrica")
                .setSubtitle("Usa tu huella digital para iniciar sesión")
                .setDescription("Coloca tu dedo en el sensor de huellas")
                .setNegativeButtonText("Usar contraseña")
                .setConfirmationRequired(false)
                .build();

        // Mostrar el prompt
        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Versión con título y subtítulo personalizados
     */
    public static void authenticate(FragmentActivity activity,
                                    String title,
                                    String subtitle,
                                    BiometricCallback callback) {
        if (!isBiometricAvailable(activity)) {
            callback.onError(getBiometricStatus(activity));
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(activity);

        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        callback.onSuccess();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode,
                                                      @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                                errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                                errorCode != BiometricPrompt.ERROR_CANCELED) {
                            callback.onError(errString.toString());
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        callback.onFailed();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText("Cancelar")
                .setConfirmationRequired(false)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}