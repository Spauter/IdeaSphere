package com.spauter.ideasphere.service.impl;

import com.spauter.extra.database.service.BaseService;
import com.spauter.extra.database.service.impl.BaseServiceImpl;
import com.spauter.ideasphere.entity.UserFollowers;
import com.spauter.ideasphere.service.UserFollowsService;
import org.springframework.stereotype.Service;

@Service("userFollowsService")
public class UserFollowsServiceImpl extends BaseServiceImpl<UserFollowers> implements UserFollowsService {
}
