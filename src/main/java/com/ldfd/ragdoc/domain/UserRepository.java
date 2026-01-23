package com.ldfd.ragdoc.domain;

import com.ldfd.ragdoc.exception.BusinessException;
import com.ldfd.ragdoc.infrastructure.mapper.UserPoMapper;
import com.ldfd.ragdoc.infrastructure.mapper.po.UserPo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final UserPoMapper userPoMapper;

    public UserPo save(UserPo userPo) {
        Assert.notNull(userPo, "userPo is required");
        return userPoMapper.save(userPo);
    }

    public UserPo update(UserPo userPo) {
        Assert.notNull(userPo, "userPo is required");
        Assert.notNull(userPo.getId(), "userPo id is required for update");

        if (!userPoMapper.existsById(userPo.getId())) {
            throw new BusinessException("404", "User not found with id: " + userPo.getId());
        }

        return userPoMapper.save(userPo);
    }

    public UserPo findById(Long id) {
        Assert.notNull(id, "id is required");

        return userPoMapper.findById(id)
                .orElseThrow(() -> new BusinessException("404", "User not found with id: " + id));
    }

    public List<UserPo> findAll() {
        return userPoMapper.findAll();
    }

    public void deleteById(Long id) {
        Assert.notNull(id, "id is required");
        if (!userPoMapper.existsById(id)) {
            throw new BusinessException("404", "User not found with id: " + id);
        }
        userPoMapper.deleteById(id);
    }
}
