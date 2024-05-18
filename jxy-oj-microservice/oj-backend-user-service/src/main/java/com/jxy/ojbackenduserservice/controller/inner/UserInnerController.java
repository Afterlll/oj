package com.jxy.ojbackenduserservice.controller.inner;

import com.jxy.ojbackendmodel.entity.User;
import com.jxy.ojbackendserviceclient.UserFeignClient;
import com.jxy.ojbackenduserservice.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * @author wangkeyao
 * 用户内部api调用，不是给前端调用的
 */
@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeignClient {
    @Resource
    private UserService userService;
    /**
     * 根据 id 获取用户
     * @param userId 用户id
     * @return
     */
    @GetMapping("/get/id")
    @Override
    public User getById(Long userId) {
        return userService.getById(userId);
    }
    /**
     * 根据 id 获取用户列表
     * @param idList
     * @return
     */
    @GetMapping("/get/ids")
    @Override
    public List<User> listByIds(Collection<Long> idList) {
        return userService.listByIds(idList);
    }
}
