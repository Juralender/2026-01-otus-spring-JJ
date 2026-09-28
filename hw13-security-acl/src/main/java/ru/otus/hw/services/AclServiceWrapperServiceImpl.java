package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AclServiceWrapperServiceImpl implements AclServiceWrapperService {

    private final MutableAclService mutableAclService;

    @Override
    @Transactional
    public void grantOwnerPermissions(Object object, String ownerUsername) {
        var objectIdentity = new ObjectIdentityImpl(object);
        MutableAcl acl = mutableAclService.createAcl(objectIdentity);

        Sid owner = new PrincipalSid(ownerUsername);
        Sid admin = new GrantedAuthoritySid("ROLE_ADMIN");

        acl.insertAce(acl.getEntries().size(), BasePermission.READ, owner, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.WRITE, owner, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.DELETE, owner, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.ADMINISTRATION, admin, true);

        mutableAclService.updateAcl(acl);
    }

    @Override
    @Transactional
    public void deleteAcl(Class<?> type, long id) {
        mutableAclService.deleteAcl(new ObjectIdentityImpl(type, id), false);
    }
}
