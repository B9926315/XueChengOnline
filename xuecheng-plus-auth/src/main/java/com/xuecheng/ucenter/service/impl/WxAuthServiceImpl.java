package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.*;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.WxAuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @Author Planck
 * @Date 2023-04-30 - 15:53
 */
@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService, WxAuthService {
    @Autowired
    private XcUserMapper xcUserMapper;
    @Autowired
    private XcUserRoleMapper xcUserRoleMapper;
    @Autowired
    private WxAuthServiceImpl currentPorxy;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${weixin.appid}")
    private String appId;
    @Value("${weixin.secret}")
    private String secret;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //账号
        String username = authParamsDto.getUsername();
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if(user==null){
            //返回空表示用户不存在
            throw new RuntimeException("账号不存在");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user,xcUserExt);
        return xcUserExt;
    }
    /**
     * 微信扫码登录逻辑
     * 1.申请令牌
     * 2.携带令牌查询用户信息
     * 3.保存用户信息到数据库
     * @param code 微信下发的授权码
     * @return User
     */
    @Override
    public XcUser wxAuth(String code) {
        //申请令牌
        Map<String, String> accessTokenMap = getAccessToken(code);
        //访问令牌
        String accessToken = accessTokenMap.get("access_token");
        String openid = accessTokenMap.get("openid");
        //携带令牌获取用户信息
        Map<String, String> userinfoMap = getUserinfo(accessToken, openid);
        //保存用户信息到数据库
        XcUser xcUser = currentPorxy.addWxUser(userinfoMap);
        return xcUser;
    }

    /**
     * 根据微信下发的授权码获得响应内容
     * 网址：https://api.weixin.qq.com/sns/oauth2/access_token?appid=身份码&secret=密钥&code=授权码&grant_type=authorization_code
     * 响应体如下：
     * {
     * "access_token":"ACCESS_TOKEN",              接口调用凭证
     * "expires_in":7200,                          access_token接口调用凭证超时时间，单位（秒）
     * "refresh_token":"REFRESH_TOKEN",            用户刷新access_token
     * "openid":"OPENID",                          授权用户唯一标识
     * "scope":"SCOPE",                            用户授权的作用域，使用逗号（,）分隔
     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"  当且仅当该网站应用已获得该用户的userinfo授权时，才会出现该字段。
     * }
     * @param code 微信下发的授权码
     * @return 响应体为JSON格式，转化为map
     */
    private Map<String,String> getAccessToken(String code){
        String urlTemplate="https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        //最终请求URL
        String url = String.format(urlTemplate, appId, secret, code);
        //远程调用此URL
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        String result = exchange.getBody();
        Map<String,String> map = JSON.parseObject(result, Map.class);
        return map;
    }

    /**
     * 携带令牌、发送http请求，获取用户基本信息
     * 响应内容
     * {
     * openid        普通用户的标识，对当前开发者帐号唯一
     * nickname        普通用户昵称
     * sex            普通用户性别，1为男性，2为女性
     * province        普通用户个人资料填写的省份
     * city            普通用户个人资料填写的城市
     * country        国家，如中国为CN
     * headimgurl        用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空
     * privilege        用户特权信息，json数组，如微信沃卡用户为（chinaunicom）
     * unionid          用户统一标识。针对一个微信开放平台帐号下的应用，同一用户的 unionid 是唯一的。
     * }
     * @param access_token 接口凭证
     * @param openid 唯一标识
     * @return map
     */
    private Map<String,String> getUserinfo(String access_token,String openid){
        String wxUrlTemplate = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String url = String.format(wxUrlTemplate, access_token, openid);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        //获取响应体结果,顺便解决乱码问题
        String result = new String(Objects.requireNonNull(exchange.getBody()).getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
        Map<String,String> map = JSON.parseObject(result, Map.class);
        return map;
    }

    /**
     * 保存用户信息到数据库
     * @param userInfoMap 用户信息Map
     */
    @Transactional
    public XcUser addWxUser(Map<String,String> userInfoMap){
        String nickname = userInfoMap.get("nickname");
        //取出唯一标识
        String unionid = userInfoMap.get("unionid");
        LambdaQueryWrapper<XcUser> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(XcUser::getWxUnionid,unionid);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if (Objects.nonNull(xcUser)) {
            return xcUser;
        }
        //向数据库写入信息
        //用户基本信息表
        xcUser=new XcUser();
        String userId = UUID.randomUUID().toString();
        xcUser.setId(userId);
        xcUser.setPassword(unionid);
        xcUser.setWxUnionid(unionid);
        xcUser.setNickname(nickname);
        xcUser.setUsername(unionid);
        xcUser.setName(nickname);
        xcUser.setUtype("101001");//学生类型
        xcUser.setStatus("1");//用户状态
        xcUser.setCreateTime(LocalDateTime.now());
        xcUserMapper.insert(xcUser);
        //向用户角色表插入信息
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        xcUserRoleMapper.insert(xcUserRole);
        return xcUser;
    }
}