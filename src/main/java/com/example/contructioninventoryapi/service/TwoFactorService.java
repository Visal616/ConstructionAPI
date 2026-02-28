package com.example.contructioninventoryapi.service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import org.springframework.stereotype.Service;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
public class TwoFactorService {

    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;

    public TwoFactorService(SecretGenerator secretGenerator, QrGenerator qrGenerator, CodeVerifier codeVerifier) {
        this.secretGenerator = secretGenerator;
        this.qrGenerator = qrGenerator;
        this.codeVerifier = codeVerifier;
    }

    public String generateNewSecret() {
        return secretGenerator.generate();
    }

    public String generateQrCodeImageUri(String secret, String email) {
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer("ConstructionAPI")
                .build();

        try {
            return getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
        } catch (QrGenerationException e) {
            throw new RuntimeException("Error generating QR code", e);
        }
    }

    public boolean isOtpValid(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }
}