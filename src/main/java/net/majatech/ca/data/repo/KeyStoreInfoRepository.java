package net.majatech.ca.data.repo;

import net.majatech.ca.data.entity.KeyStoreInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface KeyStoreInfoRepository extends JpaRepository<KeyStoreInfo, UUID> {
}
