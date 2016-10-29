package com.github.unafraid.spring.bot.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author UnAfraid
 */
@Entity
@Table(name = "users")
public class User extends AbstractEntity {
    @Column(unique = true)
    private String name;
    private Integer level;

    public User() {
    }

    public User(int id, String name, int level) {
        super(id);
        this.name = name;
        this.level = level;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
