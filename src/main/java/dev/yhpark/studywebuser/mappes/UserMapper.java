package dev.yhpark.studywebuser.mappes;

import dev.yhpark.studywebuser.entities.RegisterCodeEntity;
import dev.yhpark.studywebuser.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insertRegisterCode(RegisterCodeEntity registerCodeEntity);

    int insertUser(UserEntity user);

    RegisterCodeEntity selectRegisterCodeByEmailCodeSalt(@Param(value = "email") String email,
                                                         @Param(value = "code") String code,
                                                         @Param(value = "salt") String salt);

    UserEntity selectUserByEmail(@Param(value = "email") String email);

    UserEntity selectUserByNickname(@Param(value = "nickname") String nickname);

    UserEntity selectUserByContact(@Param(value = "contact") String contact);

    int updateRegisterCode(RegisterCodeEntity registerCodeEntity);
}












