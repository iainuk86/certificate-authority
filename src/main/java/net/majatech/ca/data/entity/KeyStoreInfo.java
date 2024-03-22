package net.majatech.ca.data.entity;

import jakarta.persistence.*;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "keystore_info")
public class KeyStoreInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "keystore_id")
    public UUID keyStoreId;

    @Column(name = "pass", nullable = false)
    public String pass;

    @Column(name = "alias", nullable = false)
    public String alias;

    @Column(name = "subject_dn", nullable = false)
    public String subjectDn;

    @Column(name = "issuer_dn", nullable = false)
    public String issuerDn;

    @Column(name = "expiry", nullable = false)
    public Instant expiry;

    @Column(name = "not_before", nullable = false)
    public Instant notBefore;

    public static KeyStoreInfo from(X509Certificate certificate, String pass, String alias) {
        KeyStoreInfo keyStoreInfo = new KeyStoreInfo();
        keyStoreInfo.setPass(pass);
        keyStoreInfo.setAlias(alias);
        keyStoreInfo.setSubjectDn(certificate.getSubjectX500Principal().toString());
        keyStoreInfo.setIssuerDn(certificate.getIssuerX500Principal().toString());
        keyStoreInfo.setNotBefore(certificate.getNotBefore().toInstant());
        keyStoreInfo.setExpiry(certificate.getNotAfter().toInstant());

        return keyStoreInfo;
    }

    public UUID getKeyStoreId() {
        return keyStoreId;
    }

    public void setKeyStoreId(UUID keyStoreId) {
        this.keyStoreId = keyStoreId;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getSubjectDn() {
        return subjectDn;
    }

    public void setSubjectDn(String subjectDn) {
        this.subjectDn = subjectDn;
    }

    public String getIssuerDn() {
        return issuerDn;
    }

    public void setIssuerDn(String issuerDn) {
        this.issuerDn = issuerDn;
    }

    public Instant getExpiry() {
        return expiry;
    }

    public void setExpiry(Instant expiry) {
        this.expiry = expiry;
    }

    public Instant getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Instant notBefore) {
        this.notBefore = notBefore;
    }
}
