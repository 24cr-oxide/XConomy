/*
 *  This file (Cache.java) is a part of project XConomy
 *  Copyright (C) YiC and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package me.yic.xconomy.data.caches;

import me.yic.xconomy.XConomy;
import me.yic.xconomy.data.GetUUID;
import me.yic.xconomy.data.caches.data.PDatas;
import me.yic.xconomy.data.caches.data.UUIDs;
import me.yic.xconomy.data.syncdata.PlayerData;
import me.yic.xconomy.utils.UUIDMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

    private static final ConcurrentHashMap<UUID, UUID> m_uuids = new ConcurrentHashMap<>();

    public static LinkedHashMap<String, BigDecimal> baltop = new LinkedHashMap<>();
    public static List<String> baltop_papi = new ArrayList<>();
    public static BigDecimal sumbalance = BigDecimal.ZERO;

    public static void insertIntoCache(final UUID uuid, final PlayerData pd) {
        if (pd != null) {
            if (pd.getName() != null && pd.getBalance() != null) {
                PDatas.put(uuid, pd);
                if (XConomy.Config.USERNAME_IGNORE_CASE) {
                    UUIDs.put(pd.getName().toLowerCase(), uuid);
                } else {
                    UUIDs.put(pd.getName(), uuid);
                }
            }
        }
    }

    public static void insertIntoMultiUUIDCache(final UUID uuid, final UUID luuid) {
        if (uuid != null && luuid != null && !uuid.toString().equals(luuid.toString())) {
            m_uuids.put(luuid, uuid);
        }
    }

    public static <T> boolean CacheContainsKey(final T key) {
        if (key instanceof UUID) {
            if (PDatas.containsKey((UUID) key)){
                return true;
            }
            if (XConomy.Config.UUIDMODE.equals(UUIDMode.SEMIONLINE)) {
                UUID luuid = getMultiUUIDCache((UUID) key);
                if (luuid == null) {
                    return false;
                }else {
                    return PDatas.containsKey(luuid);
                }
            }
            return false;
        }
        if (XConomy.Config.USERNAME_IGNORE_CASE) {
            return UUIDs.containsKey(((String) key).toLowerCase());
        }
        return UUIDs.containsKey((String) key);
    }


    public static <T> PlayerData getDataFromCache(final T key) {
        UUID u;
        if (key instanceof UUID) {
            u = (UUID) key;
            if (XConomy.Config.UUIDMODE.equals(UUIDMode.SEMIONLINE)) {
                if (!PDatas.containsKey((UUID) key) && getMultiUUIDCache((UUID) key) != null){
                    u = getMultiUUIDCache((UUID) key);
                }
            }
        } else {
            if (XConomy.Config.USERNAME_IGNORE_CASE) {
                u = UUIDs.get(((String) key).toLowerCase());
            } else {
                u = UUIDs.get(((String) key));
            }
        }
        return PDatas.get(u);
    }


    public static UUID getMultiUUIDCache(final UUID luuid) {
        if (m_uuids.containsKey(luuid)){
            return m_uuids.get(luuid);
        }
        return null;
    }

    public static void updateIntoCache(final UUID uuid, final PlayerData pd, final BigDecimal newbalance) {
        pd.setBalance(newbalance);
        PDatas.put(uuid, pd);
    }

    @SuppressWarnings("all")
    public static void removefromCache(final UUID uuid) {
        if (PDatas.containsKey(uuid)) {
            String name = PDatas.get(uuid).getName();
            PDatas.remove(uuid);
            UUIDs.remove(name);
        }
    }


    @SuppressWarnings("all")
    public static void syncOnlineUUIDCache(final String oldname, final String newname, final UUID uuid) {
        if (UUIDs.containsKey(newname)) {
            UUID u = UUIDs.get(newname);
            PDatas.remove(u);
            UUIDs.remove(newname);
        }
        GetUUID.removeUUIDFromCache(oldname);
        GetUUID.removeUUIDFromCache(newname);
        removefromCache(uuid);
    }


    public static void clearCache(boolean redis) {
        PDatas.clear(redis);
        UUIDs.clear(redis);
        m_uuids.clear();
    }


}