package org.acme.quarkus.sample;

import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

/**
 * Todo
 */
@Entity
public class Todo extends PanacheEntity {
    public String title;
    public Boolean completed;
}
