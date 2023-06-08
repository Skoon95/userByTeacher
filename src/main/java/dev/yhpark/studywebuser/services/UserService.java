package dev.yhpark.studywebuser.services;

import dev.yhpark.studywebuser.entities.RegisterCodeEntity;
import dev.yhpark.studywebuser.entities.UserEntity;
import dev.yhpark.studywebuser.enums.user.LoginResult;
import dev.yhpark.studywebuser.enums.user.RegisterResult;
import dev.yhpark.studywebuser.enums.user.RegisterSendEmailResult;
import dev.yhpark.studywebuser.enums.user.RegisterVerifyEmailResult;
import dev.yhpark.studywebuser.mappes.UserMapper;
import dev.yhpark.studywebuser.utils.CryptoUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
public class UserService {
    private final JavaMailSender javaMailSender;
    private final UserMapper userMapper;

    @Autowired
    public UserService(JavaMailSender javaMailSender, UserMapper userMapper) {
        this.javaMailSender = javaMailSender;
        this.userMapper = userMapper;
    }

    public LoginResult login(UserEntity user)
            throws NoSuchAlgorithmException {
        UserEntity existingUser = this.userMapper.selectUserByEmail(user.getEmail());
        if (existingUser == null) {
            return LoginResult.FAILURE;
        }
        user.setPassword(CryptoUtil.hashSha512(user.getPassword()));
        if (!user.getPassword().equals(existingUser.getPassword())) {
            return LoginResult.FAILURE;
        }
        user.setNickname(existingUser.getNickname())
                .setAddressPostal(existingUser.getAddressPostal())
                .setAddressPrimary(existingUser.getAddressPrimary())
                .setAddressSecondary(existingUser.getAddressSecondary())
                .setGender(existingUser.getGender())
                .setName(existingUser.getName())
                .setBirth(existingUser.getBirth())
                .setContactProvider(existingUser.getContactProvider())
                .setContact(existingUser.getContact())
                .setRegisteredAt(existingUser.getRegisteredAt());
        return LoginResult.SUCCESS;
    }

    public RegisterResult register(RegisterCodeEntity registerCode, UserEntity user) throws NoSuchAlgorithmException {
        registerCode = this.userMapper.selectRegisterCodeByEmailCodeSalt(
                registerCode.getEmail(),
                registerCode.getCode(),
                registerCode.getSalt());
        if (registerCode == null || !registerCode.isVerified()) {
            return RegisterResult.FAILURE_EMAIL_NOT_VERIFIED;
        }
        if (this.userMapper.selectUserByEmail(user.getEmail()) != null) {
            return RegisterResult.FAILURE_EMAIL_DUPLICATE;
        }
        if (this.userMapper.selectUserByNickname(user.getNickname()) != null) {
            return RegisterResult.FAILURE_NICKNAME_DUPLICATE;
        }
        if (this.userMapper.selectUserByContact(user.getContact()) != null) {
            return RegisterResult.FAILURE_CONTACT_DUPLICATE;
        }
        user.setPassword(CryptoUtil.hashSha512(user.getPassword()));
        return this.userMapper.insertUser(user) > 0
                ? RegisterResult.SUCCESS
                : RegisterResult.FAILURE;
    }

    public RegisterSendEmailResult registerSendEmail(RegisterCodeEntity registerCodeEntity)
            throws NoSuchAlgorithmException {
        // 1. 전달 받은 email 기준으로 UserEntity를 받아온다.
        // 2. <1>에서 받아온 UserEntity가 null이 아닐 경우, 이미 사용중인 이메일임으로 이에 맞는 결과값을 반환한다.

        UserEntity existingUserEntity = this.userMapper.selectUserByEmail(registerCodeEntity.getEmail());
        if (existingUserEntity != null) {
            return RegisterSendEmailResult.FAILURE_EMAIL_DUPLICATE;
        }
        String code = RandomStringUtils.randomNumeric(6);
        String salt = String.format("%s%s%f%f",
                registerCodeEntity.getEmail(),
                code,
                Math.random(),
                Math.random());
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt.getBytes(StandardCharsets.UTF_8));
        salt = String.format("%0128x", new BigInteger(1, md.digest()));
        registerCodeEntity
                .setCode(code)
                .setSalt(salt)
                .setCreatedAt(new Date())
                .setExpiresAt(DateUtils.addMinutes(registerCodeEntity.getCreatedAt(), 10))
                .setVerified(false);
        int insertResult = this.userMapper.insertRegisterCode(registerCodeEntity);

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(registerCodeEntity.getEmail());
        mail.setSubject("[Study Web] 인증번호");
        mail.setText(code);
        this.javaMailSender.send(mail);

        return insertResult > 0
                ? RegisterSendEmailResult.SUCCESS
                : RegisterSendEmailResult.FAILURE;
    }

    public RegisterVerifyEmailResult registerVerifyEmail(RegisterCodeEntity registerCodeEntity) {
        RegisterCodeEntity existingRegisterCodeEntity = this.userMapper.selectRegisterCodeByEmailCodeSalt(
                registerCodeEntity.getEmail(),
                registerCodeEntity.getCode(),
                registerCodeEntity.getSalt());
        if (existingRegisterCodeEntity == null) {
            return RegisterVerifyEmailResult.FAILURE;
        }
        Date currentDate = new Date();
        if (currentDate.compareTo(existingRegisterCodeEntity.getExpiresAt()) > 0) {
            return RegisterVerifyEmailResult.FAILURE_EXPIRED;
        }
        existingRegisterCodeEntity.setVerified(true);
        return this.userMapper.updateRegisterCode(existingRegisterCodeEntity) > 0
                ? RegisterVerifyEmailResult.SUCCESS
                : RegisterVerifyEmailResult.FAILURE;
    }
}














