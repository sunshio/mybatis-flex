package com.mybatisflex.coretest;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.KeyType;

import java.util.Date;

@Table("tb_article")
public class Article {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Id(keyType = KeyType.Generator, value = "uuid")
    private String uuid;

    private Long accountId;

    private String title;

    @Column(isLarge = true)
    private String content;

    @Column(onInsertValue = "now()")
    private Date created;

    @Column(onUpdateValue = "now()", onInsertValue = "now()")
    private Date modified;

    @Column(isLogicDelete = true)
    private Boolean isDelete;

    @Column(version = true)
    private Long version;

    private Account account;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Boolean getDelete() {
        return isDelete;
    }

    public void setDelete(Boolean delete) {
        isDelete = delete;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
