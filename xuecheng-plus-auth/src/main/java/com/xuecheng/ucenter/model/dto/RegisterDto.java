package com.xuecheng.ucenter.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @Author Planck
 * @Date 2023-05-02 - 9:56
 */
@Data
@ToString
public class RegisterDto {
    private String cellphone;
    private String checkcode;
    private String checkcodekey;

    private String confirmpwd;

    private String email;

    private String nickname;

    private String password;

    private String username;
}
