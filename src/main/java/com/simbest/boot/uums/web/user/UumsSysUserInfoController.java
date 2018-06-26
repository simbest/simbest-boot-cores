package com.simbest.boot.uums.web.user;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.uums.api.user.UumsSysUserinfoApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Api (description = "系统用户操作相关接口")
@Slf4j
@RestController
@RequestMapping("/uums/sys/userinfo")
public class UumsSysUserInfoController {

    @Autowired
    private UumsSysUserinfoApi uumsSysUserinfoApi;

    /**
     * 获取角色信息列表并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param map
     * @return
     */
    @ApiOperation(value = "获取角色信息列表并分页", notes = "获取角色信息列表并分页")
    @ApiImplicitParams ({ //
            @ApiImplicitParam (name = "page", value = "当前页码", dataType = "int", paramType = "query", //
                    required = true, example = "1"), //
            @ApiImplicitParam(name = "size", value = "每页数量", dataType = "int", paramType = "query", //
                    required = true, example = "10"), //
            @ApiImplicitParam(name = "direction", value = "排序规则（asc/desc）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "properties", value = "排序规则（属性名称）", dataType = "String", //
                    paramType = "query") //
    })
    @PostMapping("/findAll")
    public JsonResponse findAll( @RequestParam(required = false, defaultValue = "1") int page, //
                                 @RequestParam(required = false, defaultValue = "10") int size, //
                                 @RequestParam(required = false) String direction, //
                                 @RequestParam(required = false) String properties,
                                 @RequestParam String appcode,
                                 @RequestBody Map map ) {
        return JsonResponse.success(uumsSysUserinfoApi.findAll(page,size,direction,properties,appcode,map));
    }

    /**
     * 获取角色信息列表并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @return
     */
    @ApiOperation(value = "获取角色信息列表并分页", notes = "获取角色信息列表并分页")
    @ApiImplicitParams ({ //
            @ApiImplicitParam (name = "page", value = "当前页码", dataType = "int", paramType = "query", //
                    required = true, example = "1"), //
            @ApiImplicitParam(name = "size", value = "每页数量", dataType = "int", paramType = "query", //
                    required = true, example = "10"), //
            @ApiImplicitParam(name = "direction", value = "排序规则（asc/desc）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "properties", value = "排序规则（属性名称）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "appcode", value = "当前应用appcode", dataType = "String", //
                    paramType = "query")
    })
    @PostMapping("/findAllUsers")
    public JsonResponse findAllUsers( @RequestParam(required = false, defaultValue = "1") int page, //
                                 @RequestParam(required = false, defaultValue = "10") int size, //
                                 @RequestParam(required = false) String direction, //
                                 @RequestParam(required = false) String properties,
                                 @RequestParam String appcode ) {
        return JsonResponse.success(uumsSysUserinfoApi.findAllUsers(page,size,direction,properties,appcode));
    }


    /**
     *根据用户名查询用户信息
     * @param username
     * @return
     */
    @ApiOperation(value = "根据用户名查询用户信息", notes = "根据用户名查询用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "appcode", dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/findByUsername")
    public JsonResponse findByUsername(@RequestParam String username,@RequestParam String appcode) {
        return JsonResponse.success(uumsSysUserinfoApi.findByUsername(username,appcode));
    }

    /**
     * 根据角色id获取用户但不分页
     * @param roleId
     * @return
     */
    @ApiOperation(value = "根据角色id获取用户但不分页", notes = "根据角色id获取用户但不分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleId", value = "roleId", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "appcode", dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/findUserByRoleNoPage")
    public JsonResponse findUserByRoleNoPage( @RequestParam Integer roleId , @RequestParam String appcode){
        return JsonResponse.success(uumsSysUserinfoApi.findUserByRoleNoPage(roleId,appcode ));
    }

    /**
     * 根据过滤条件获取决策下的用户
     * @param appcode
     * @param sysAppDecisionMap
     * @return
     */
    @ApiOperation(value = "根据过滤条件获取决策下的用户", notes = "根据过滤条件获取决策下的用户")
    @ApiImplicitParams ({
            @ApiImplicitParam(name = "appcode", value = "当前应用appcode", dataType = "String" ,paramType = "query")
    })
    @PostMapping(value ="/findUserByDecisionNoPage")
    public JsonResponse findUserByDecisionNoPage(@RequestParam String appcode,@RequestBody Map sysAppDecisionMap){
        return JsonResponse.success(uumsSysUserinfoApi.findUserByDecisionNoPage(appcode,sysAppDecisionMap));
    }

    /*    @ApiOperation(value = "查询一个应用下参与的全部用户，包含用户所在的组织以及用户的职位信息分页", notes = "查询一个应用下参与的全部用户，包含用户所在的组织以及用户的职位信息分页")
    @ApiImplicitParams ({ //
            @ApiImplicitParam (name = "page", value = "当前页码", dataType = "int", paramType = "query", //
                    required = true, example = "1"), //
            @ApiImplicitParam(name = "size", value = "每页数量", dataType = "int", paramType = "query", //
                    required = true, example = "10"), //
            @ApiImplicitParam(name = "direction", value = "排序规则（asc/desc）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "properties", value = "排序规则（属性名称）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "appcode", value = "appcode", dataType = "String", //
                    paramType = "query"), //
    })
    @PostMapping(value ="/findUserByApp")
    public JsonResponse findUserByApp(@RequestParam(required = false, defaultValue = "1") int page, //
                                      @RequestParam(required = false, defaultValue = "10") int size, //
                                      @RequestParam(required = false) String direction, //
                                      @RequestParam(required = false) String properties,
                                      @RequestParam String appcode,
                                      @RequestBody Map map){
        return JsonResponse.success(uumsSysUserinfoApi.findUserByApp(page,size,direction,properties,appcode,map));
    }




    @ApiOperation(value = "查询一个应用下参与的全部用户，包含用户所在的组织以及用户的职位信息不分页", notes = "查询一个应用下参与的全部用户，包含用户所在的组织以及用户的职位信息不分页")
    @ApiImplicitParam(name = "appcode", value = "appcode", dataType = "String" ,paramType = "query")
    @PostMapping(value ="/findUserByAppNoPage")
    public JsonResponse findUserByAppNoPage(@RequestParam String appcode,@RequestBody Map map){
        return JsonResponse.success(uumsSysUserinfoApi.findUserByAppNoPage(appcode,map));
    }*/


}
