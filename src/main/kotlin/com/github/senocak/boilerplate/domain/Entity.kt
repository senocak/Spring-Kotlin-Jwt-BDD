package com.github.senocak.boilerplate.domain

import com.github.senocak.boilerplate.util.RoleName
import org.hibernate.annotations.GenericGenerator
import java.io.Serializable
import java.util.Date
import java.util.UUID
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@MappedSuperclass
open class BaseDomain(
        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        @Column(name = "id", updatable = false, nullable = false)
        var id: String? = null,
        @Column var createdAt: Date = Date(),
        @Column var updatedAt: Date = Date()
): Serializable {
        @PrePersist
        protected open fun prePersist() {
                id = UUID.randomUUID().toString()
                // TODO: add snowflake
        }
}

@Entity
@Table(name = "users", uniqueConstraints = [
        UniqueConstraint(columnNames = ["username"]),
        UniqueConstraint(columnNames = ["email"])
])
class User(
        @Column var name: String,
        @Column var username: String,
        @Column var email : String,
        @Column var password : String?,
        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(
            name = "user_roles",
            joinColumns = [JoinColumn(name = "user_id")],
            inverseJoinColumns = [JoinColumn(name = "role_id")]
        )
        var roles: MutableList<Role> = ArrayList()
): BaseDomain()

@Entity
@Table(name = "roles")
class Role(@Column @Enumerated(EnumType.STRING) var name: RoleName? = null): BaseDomain()
