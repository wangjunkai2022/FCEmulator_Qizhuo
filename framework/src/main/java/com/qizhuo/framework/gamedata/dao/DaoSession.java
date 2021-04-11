package com.qizhuo.framework.gamedata.dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.qizhuo.framework.gamedata.dao.entity.GameEntity;
import com.qizhuo.framework.gamedata.dao.entity.RecentGameEntity;

import com.qizhuo.framework.gamedata.dao.GameEntityDao;
import com.qizhuo.framework.gamedata.dao.RecentGameEntityDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig gameEntityDaoConfig;
    private final DaoConfig recentGameEntityDaoConfig;

    private final GameEntityDao gameEntityDao;
    private final RecentGameEntityDao recentGameEntityDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        gameEntityDaoConfig = daoConfigMap.get(GameEntityDao.class).clone();
        gameEntityDaoConfig.initIdentityScope(type);

        recentGameEntityDaoConfig = daoConfigMap.get(RecentGameEntityDao.class).clone();
        recentGameEntityDaoConfig.initIdentityScope(type);

        gameEntityDao = new GameEntityDao(gameEntityDaoConfig, this);
        recentGameEntityDao = new RecentGameEntityDao(recentGameEntityDaoConfig, this);

        registerDao(GameEntity.class, gameEntityDao);
        registerDao(RecentGameEntity.class, recentGameEntityDao);
    }
    
    public void clear() {
        gameEntityDaoConfig.clearIdentityScope();
        recentGameEntityDaoConfig.clearIdentityScope();
    }

    public GameEntityDao getGameEntityDao() {
        return gameEntityDao;
    }

    public RecentGameEntityDao getRecentGameEntityDao() {
        return recentGameEntityDao;
    }

}
