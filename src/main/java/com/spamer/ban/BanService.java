package com.spamer.ban;

import com.spamer.domain.BannedTargetEntity;
import com.spamer.repository.BannedTargetRepository;
import com.spamer.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BanService {

    private final BannedTargetRepository bannedTargetRepository;
    private final UserRepository userRepository;

    public BanService(BannedTargetRepository bannedTargetRepository, UserRepository userRepository) {
        this.bannedTargetRepository = bannedTargetRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public void ensureNotBanned(String target) {
        if (bannedTargetRepository.existsByTarget(target)) {
            throw new IllegalArgumentException("Target is banned: " + target);
        }
    }

    @Transactional
    public BannedTargetEntity ban(String target, String reason, String operatorUsername) {
        if (bannedTargetRepository.existsByTarget(target)) {
            throw new IllegalArgumentException("Target already banned");
        }

        Long operatorId = userRepository.findByUsername(operatorUsername)
                .map(u -> u.getId())
                .orElseThrow(() -> new IllegalArgumentException("Operator not found"));

        BannedTargetEntity banned = new BannedTargetEntity();
        banned.setTarget(target);
        banned.setReason(reason);
        banned.setBannedByUserId(operatorId);
        return bannedTargetRepository.save(banned);
    }

    @Transactional(readOnly = true)
    public List<BannedTargetEntity> listAll() {
        return bannedTargetRepository.findAll();
    }

    @Transactional
    public void unban(Long id) {
        bannedTargetRepository.deleteById(id);
    }
}
