package com.Shakwa.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    
    @Value("${mail.enabled:true}")
    private boolean mailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public boolean isMailEnabled() {
        return mailEnabled;
    }

    public void sendOtpEmail(String to, String otpCode) {
        // Development mode: Log OTP instead of sending email
        if (!mailEnabled) {
            logger.warn("========================================");
            logger.warn("MAIL DISABLED - OTP FOR DEVELOPMENT");
            logger.warn("Email: {}", to);
            logger.warn("OTP Code: {}", otpCode);
            logger.warn("========================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("رمز التحقق من البريد الإلكتروني - Shakwa");
            message.setText(buildOtpEmailBody(otpCode));
            
            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", to);
        } catch (org.springframework.mail.MailAuthenticationException e) {
            logger.error("Mail authentication failed. Please check your email credentials in application.properties", e);
            logger.warn("Falling back to development mode - OTP: {} for email: {}", otpCode, to);
            // Don't throw exception in development - just log the OTP
            if (mailEnabled) {
                throw new RuntimeException("Failed to send OTP email: Mail authentication failed. Please configure email settings correctly.", e);
            }
        } catch (org.springframework.mail.MailSendException e) {
            logger.error("Failed to send OTP email to: {}. Check SMTP settings and network connection.", to, e);
            logger.warn("Falling back to development mode - OTP: {} for email: {}", otpCode, to);
            // Don't throw exception in development - just log the OTP
            if (mailEnabled) {
                throw new RuntimeException("Failed to send OTP email: SMTP error. Please check email configuration.", e);
            }
        } catch (Exception e) {
            logger.error("Failed to send OTP email to: {}", to, e);
            logger.warn("Falling back to development mode - OTP: {} for email: {}", otpCode, to);
            // Don't throw exception in development - just log the OTP
            if (mailEnabled) {
                throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
            }
        }
    }

    private String buildOtpEmailBody(String otpCode) {
        return String.format("""
            مرحباً بك في نظام شكوى!
            
            رمز التحقق من البريد الإلكتروني الخاص بك هو:
            
            %s
            
            هذا الرمز صالح لمدة 10 دقائق فقط.
            
            إذا لم تطلب هذا الرمز، يرجى تجاهل هذه الرسالة.
            
            مع تحيات فريق شكوى
            """, otpCode);
    }
}

