// AnotherBean.java - A bean without a @Table annotation
package org.simpledao;

import lombok.Data;

@Data
public class AnotherBean {
    private int anotherBeanId; // Guessed as update key
    private String description;
}