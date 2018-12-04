package com.mvc.demo.shiro.realm;

import com.mvc.demo.entity.UserInfo;
import com.mvc.demo.mapper.UserInfoMapper;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dawei 2018/12/1
 */
@Component
public class MyShiroRealm extends AuthorizingRealm {


    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * 授权
     *
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        //1、通过主体获取用户名
        String loginName = (String) principals.getPrimaryPrincipal();
        //2、通过用户名获取 角色信息
        //Set<String> userRolesByUserName = userInfoMapper.getUserRolesByUserName(userName);
        Set<String> userRolesByUserName = getUserRolesByUserName(loginName);
        //3、通过用户名获取 权限数据
        //Set<String> userPermissionByUserName = userInfoMapper.getUserPermissionByUserName(userName);
        Set<String> userPermissionByUserName = getUserPermissionByUserName(loginName);

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.setRoles(userRolesByUserName);
        authorizationInfo.setStringPermissions(userPermissionByUserName);
        return authorizationInfo;
    }

    /**
     * 认证
     * 创建SecuityManager --> 主体提交认证 --> SecurityManager进行认证 -->Authentictor进行认证判断 --> 认证数据依赖于Realm
     *
     * @param authenticationToken 主体获取的认证信息
     * @return
     * @throws AuthenticationException 认证失败的异常
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //1、从主题传过来的认证信息中 获取用户名
        String loginName = (String) authenticationToken.getPrincipal();
        //2、通过用户名在数据库中获取凭证
        UserInfo userInfo = userInfoMapper.getUserInfoByLoginName(loginName);
        if (userInfo != null && !StringUtils.isEmpty(userInfo.getPassWord())) {
            SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(loginName, userInfo.getPassWord(), "myShiroRealm");
            //加盐进行加密
            authenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(userInfo.getMakeSalt()));
            return authenticationInfo;
        }
        return null;
    }



    private Set<String> getUserRolesByUserName(String userName) {
        Set<String> roles = new HashSet<>();
        roles.add("admin");
        roles.add("user");
        return roles;
    }

    private Set<String> getUserPermissionByUserName(String userName) {
        Set<String> permissionSet = new HashSet<>();
        permissionSet.add("user:update");
        permissionSet.add("user:create");
        return permissionSet;
    }
}
