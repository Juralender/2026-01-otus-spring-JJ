package ru.otus.hw.services;

public interface AclServiceWrapperService {

    void grantOwnerPermissions(Object object, String ownerUsername);

    void deleteAcl(Class<?> type, long id);
}
