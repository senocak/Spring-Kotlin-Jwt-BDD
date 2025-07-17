package com.github.senocak.boilerplate.service

import com.github.senocak.boilerplate.domain.Role
import com.github.senocak.boilerplate.repository.RoleRepository
import com.github.senocak.boilerplate.util.RoleName
import org.springframework.stereotype.Service

@Service
class RoleService(private val roleRepository: RoleRepository) {

    /**
     * @param roleName -- enum variable to retrieve from db
     * @return -- Role object retrieved from db
     */
    fun findByName(roleName: RoleName): Role? =
        roleRepository.findByName(roleName = roleName)
}
