package org.resthub.identity.controller;

import java.util.List;

import javax.inject.Inject;
import org.resthub.common.exception.NotFoundException;

import org.resthub.identity.exception.AlreadyExistingEntityException;
import org.resthub.identity.exception.ExpectationFailedException;
import org.resthub.identity.model.Group;
import org.resthub.identity.model.Role;
import org.resthub.identity.model.User;
import org.resthub.identity.service.GroupService;
import org.resthub.identity.service.RoleService;
import org.resthub.identity.service.UserService;
import org.resthub.web.controller.ServiceBasedRestController;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Front controller for Group Management<br/>
 * Only ADMINS can access to this API
 */
@Controller @RequestMapping("/api/group")
public class GroupController extends ServiceBasedRestController<Group, Long, GroupService> {

    /**
     * The userService <br/>
     * This should be a bean <br/>
     * This class need it to deal properly with user, eg to add a {@Link
     * User} to a {@Link Group}
     * 
     * */
	
	
	@Inject
    RoleService roleService;
	
	@Inject
    UserService userService;

    @Inject @Override      
    public void setService(GroupService service) {
        this.service = service;
    }

    /**
     * Automatically called to inject the userService beans<br/>
     * This class need it to deal properly with user <br/>
     * 
     * @param userService
     *            the userService bean
     * */
    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /** Override this methods in order to secure it **/
    
    
    @Secured({ "IM_GROUP_ADMIN" }) @Override
    public Group create(@RequestBody Group group) {
    	try{
    		return super.create(group);
    	}catch(AlreadyExistingEntityException e){
    		throw new ExpectationFailedException(e.getMessage());
    	}
    }
    

    /** Override this methods in order to secure it **/
    
    
    @Secured({ "IM_GROUP_ADMIN" }) @Override 
    public Group update(@PathVariable("id") Long id, @RequestBody Group group) {
        try{
        	return super.update(id, group);
    	}catch(AlreadyExistingEntityException e){
    		throw new ExpectationFailedException(e.getMessage());
    	}
    }
    
    
    /** Override this methods in order to secure it **/
    @Secured({ "IM_GROUP_ADMIN", "IM_GROUP_READ" }) @Override
    public Iterable<Group> findAll() {
        return super.findAll();
    }
    
    /** Override this methods in order to secure it **/
    @Secured({ "IM_GROUP_ADMIN", "IM_GROUP_READ" }) @RequestMapping(value = "/findAllPerPage", method = RequestMethod.GET) @Override
    public Page<Group> findPaginated(@RequestParam(value="page", required=false) Integer page, @RequestParam(value="size", required=false)  Integer size) {
        return super.findPaginated(page, size);
    }
    
    /** Override this methods in order to secure it **/
    @Secured({ "IM_GROUP_ADMIN", "IM_GROUP_READ" }) @Override
    public Group findById(@PathVariable("id") Long id) {
        return super.findById(id);
    }
    
    /** Override this methods in order to secure it **/
    @Secured({ "IM_GROUP_ADMIN" }) @Override
    public void delete() {
        super.delete();
    }
    
    /** Override this methods in order to secure it **/
    @Secured({ "IM_GROUP_ADMIN" }) @Override
    public void delete(@PathVariable(value = "id") Long id) {
        super.delete(id);
    }

    /**
     * Find the group identified by the specified name.<br/>
     * 
     * @param name
     *            the name of the group
     * @return the group, in XML or JSON if the group can be found otherwise
     *         HTTP Error 404
     */
    @RequestMapping(method = RequestMethod.GET, value = "name/{name}") @ResponseBody
    public Group getGroupByName(@PathVariable("name") String name) {
        Group group = this.service.findByName(name);
        if (group == null) {
            throw new NotFoundException();
        }
        return group;
    }

    /**
     * Gets the groups depending of the group
     * 
     * @param name
     *            the name of the group to search insides groups
     * 
     * @return a list of group, in XML or JSON if the group can be found
     *         otherwise HTTP Error 404
     */
    @RequestMapping(method = RequestMethod.GET, value = "name/{name}/groups") @ResponseBody
    public List<Group> getGroupsFromGroups(@PathVariable("name") String name) {
        return this.service.getGroupsFromGroup(name);
    }

    /**
     * Puts a group inside the groups lists of one other group
     * 
     * @param name
     *            the name of the group in which we should add a group
     * @param group
     *            the name of the group the be added
     */
    @RequestMapping(method = RequestMethod.PUT, value = "name/{name}/groups/{group}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addGroupToGroup(@PathVariable("name") String name, @PathVariable("group") String group) {
        this.service.addGroupToGroup(name, group);
    }

    /**
     * Deletes a group from the groups lists of one other group
     * 
     * @param name
     *            the name of the group in which we should remove a group
     * @param group
     *            the name of the gorup the be removed
     */    
    @RequestMapping(method = RequestMethod.DELETE, value = "name/{name}/groups/{groups}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeGroupsForGroup(@PathVariable("name") String name, @PathVariable("groups") String groupName) {
        this.service.removeGroupFromGroup(name, groupName);
    }

    /**
     * Gets the permissions of one group
     * 
     * @param name
     *            the name of the group to search insides groups
     * 
     * @return a list of permissions, in XML or JSON if the group can be found
     *         otherwise HTTP Error 404
     */
    @RequestMapping(method = RequestMethod.GET, value = "/name/{name}/permissions") @ResponseBody
    public List<String> getPermisionsFromGroup(@PathVariable("name") String name) {
        List<String> permissions = this.service.getGroupDirectPermissions(name);
        if (permissions == null) {
            throw new NotFoundException();
        }
        return permissions;
    }

    /**
     * Add a permission to a group
     * 
     * @param name
     *            the name of the group in which we should add a group
     * @param permission
     *            the permission to be added
     */
    @RequestMapping(method = RequestMethod.PUT, value = "name/{name}/permissions/{permission}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addPermissionsToUser(@PathVariable("name") String login, @PathVariable("permission") String permission) {
        this.service.addPermissionToGroup(login, permission);
    }

    /**
     * Remove a permission form one Group
     * 
     * @param name
     *            the name of the group in which we should remove a permission
     * @param permisssion
     *            the permission to be removed
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "name/{name}/permissions/{permission}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePermissionsFromUser(@PathVariable("name") String name, @PathVariable("permission") String permission) {
        this.service.removePermissionFromGroup(name, permission);
    }

    @RequestMapping(method = RequestMethod.GET, value = "name/{name}/users") @ResponseBody
    public List<User> getUsersFromGroup(@PathVariable("name") String name) {
        List<User> usersFromGroup = this.userService.getUsersFromGroup(name);
        if (usersFromGroup == null) {
            throw new NotFoundException();
        }
        return usersFromGroup;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "name/{name}/roles") @ResponseBody
    public List<Role> getRolesFromGroup(@PathVariable("name") String name) {
        List<Role> roles = this.service.getRolesFromGroup(name);
        if (roles == null) {
            throw new NotFoundException();
        }
        return roles;
    }
    
    @Secured({ "IM_GROUP_ADMIN" }) @RequestMapping(method = RequestMethod.PUT, value = "name/{name}/roles/{role}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addRoleToGroup(@PathVariable("name") String name, @PathVariable("role") String role) {
        this.service.addRoleToGroup(name, role);
    }
    
    @Secured({ "IM_GROUP_ADMIN" }) @RequestMapping(method = RequestMethod.DELETE, value = "name/{name}/roles/{role}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRoleFromGroup(@PathVariable("name") String name, @PathVariable("role") String role) {
        this.service.removeRoleFromGroup(name, role);
    }
    
    
    @Secured({ "IM_GROUP_ADMIN" }) @RequestMapping(method = RequestMethod.DELETE, value = "name/{name}/roles") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAllRoleFromGroup(@PathVariable("name") String name) {
    	 List<Role> roles = this.service.getRolesFromGroup(name);
         if (roles == null) {
             throw new NotFoundException();
         }
        for (Role r : roles){
        	this.service.removeRoleFromGroup(name, r.getName());
        }
        
    }
}
