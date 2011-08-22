package ch.ralscha.starter.service;

import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.FORM_POST;
import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.STORE_MODIFY;
import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.STORE_READ;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ch.ralscha.extdirectspring.annotation.ExtDirectMethod;
import ch.ralscha.extdirectspring.bean.ExtDirectResponse;
import ch.ralscha.extdirectspring.bean.ExtDirectResponseBuilder;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreReadRequest;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreResponse;
import ch.ralscha.extdirectspring.filter.StringFilter;
import ch.ralscha.starter.entity.QUser;
import ch.ralscha.starter.entity.Role;
import ch.ralscha.starter.entity.User;
import ch.ralscha.starter.repository.RoleRepository;
import ch.ralscha.starter.repository.UserCustomRepository;
import ch.ralscha.starter.repository.UserRepository;
import ch.ralscha.starter.util.Util;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.mysema.query.BooleanBuilder;

@Service
@Controller
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserCustomRepository userCustomRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MessageSource messageSource;

	@ExtDirectMethod(STORE_READ)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ExtDirectStoreResponse<User> load(ExtDirectStoreReadRequest request) {

		String filterValue = null;
		if (!request.getFilters().isEmpty()) {
			StringFilter filter = (StringFilter) request.getFilters().iterator().next();
			filterValue = filter.getValue();
		}
		
		Page<User> page = userCustomRepository.findWithFilter(filterValue, Util.createPageRequest(request));
		return new ExtDirectStoreResponse<User>((int) page.getTotalElements(), page.getContent());
	}

	@ExtDirectMethod(STORE_READ)
	@PreAuthorize("isAuthenticated()")
	public List<Role> loadAllRoles() {
		return roleRepository.findAll();
	}

	@ExtDirectMethod(STORE_MODIFY)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void destroy(List<User> destroyUsers) {
		for (User user : destroyUsers) {
			userRepository.delete(user);
		}
	}

	@ExtDirectMethod(FORM_POST)
	@ResponseBody
	@RequestMapping(value = "/userFormPost", method = RequestMethod.POST)
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ExtDirectResponse userFormPost(HttpServletRequest request, Locale locale,
			@RequestParam(required = false) String roleIds, @Valid User modifiedUser, BindingResult result) {

		//Check uniqueness of userName and email
		if (!result.hasErrors()) {
			BooleanBuilder bb = new BooleanBuilder(QUser.user.userName.equalsIgnoreCase(modifiedUser.getUserName()));
			if (modifiedUser.getId() != null) {
				bb.and(QUser.user.id.ne(modifiedUser.getId()));
			}
			if (userRepository.count(bb) > 0) {
				result.rejectValue("userName", null, messageSource.getMessage("user_usernametaken", null, locale));
			}

			bb = new BooleanBuilder(QUser.user.email.equalsIgnoreCase(modifiedUser.getEmail()));
			if (modifiedUser.getId() != null) {
				bb.and(QUser.user.id.ne(modifiedUser.getId()));
			}
			if (userRepository.count(bb) > 0) {
				result.rejectValue("email", null, messageSource.getMessage("user_emailtaken", null, locale));
			}
		}

		if (!result.hasErrors()) {

			if (StringUtils.hasText(modifiedUser.getPasswordHash())) {
				modifiedUser.setPasswordHash(passwordEncoder.encode(modifiedUser.getPasswordHash()));
			}

			Set<Role> roles = Sets.newHashSet();
			if (StringUtils.hasText(roleIds)) {
				Iterable<String> roleIdsIt = Splitter.on(",").split(roleIds);
				for (String roleId : roleIdsIt) {
					roles.add(roleRepository.findOne(Long.valueOf(roleId)));
				}
			}

			if (modifiedUser.getId() != null) {
				User dbUser = userRepository.findOne(modifiedUser.getId());
				if (dbUser != null) {
					dbUser.getRoles().clear();
					dbUser.getRoles().addAll(roles);
					dbUser.update(modifiedUser);
				}
			} else {
				modifiedUser.setCreateDate(new Date());
				modifiedUser.setRoles(roles);
				userRepository.save(modifiedUser);
			}
		}

		ExtDirectResponseBuilder builder = new ExtDirectResponseBuilder(request);
		builder.addErrors(result);
		return builder.build();

	}

}