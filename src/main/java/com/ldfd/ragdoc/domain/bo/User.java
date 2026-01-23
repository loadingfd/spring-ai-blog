package com.ldfd.ragdoc.domain.bo;

import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Data
@ToString(doNotUseGetters = true)
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    Long id;
    String username;
    String email;
    String password;
    String fullName;
    Instant createdAt;
    Instant updatedAt;
}
