package com.gomeplus.im.manage.dao;


import com.gomeplus.im.manage.model.Contacts;

/**
 * Created by wangshikai on 2016/2/22.
 */
public interface ContactsMapper {

    public Contacts getContacts(long userId);

}
