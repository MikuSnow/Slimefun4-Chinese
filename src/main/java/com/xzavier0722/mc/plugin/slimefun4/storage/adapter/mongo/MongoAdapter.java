package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mongo;

import com.google.gson.internal.LinkedTreeMap;
import com.mengcraft.simpleorm.MongoWrapper;
import com.mengcraft.simpleorm.ORM;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlCommonAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.*;
import heypixel.com.mongodb.BasicDBObject;
import heypixel.com.mongodb.DBCursor;
import heypixel.com.mongodb.DBObject;
import io.github.bakedlibs.dough.collections.Pair;

import java.util.*;
import java.util.regex.Pattern;

public class MongoAdapter extends SqlCommonAdapter<MongoConfig> {

    private static MongoWrapper.MongoDatabaseWrapper playerProfile;
    private static MongoWrapper.MongoDatabaseWrapper backpackInventory;
    private static MongoWrapper.MongoDatabaseWrapper blockData;
    private static MongoWrapper.MongoDatabaseWrapper chunkData;

    @Override
    public void initStorage(DataType type) {
        MongoWrapper mongoWrapper = ORM.globalMongoWrapper();
        mongoWrapper.ping();
        if (playerProfile == null) {
            playerProfile = mongoWrapper.open(
                "survival_slimefun",
                "player_profile"
            );
        }
        playerProfile.open(dbCollection -> {
            dbCollection.createIndex(
                new BasicDBObject("_id", ""),
                (new BasicDBObject("name", "_id")).append("unique", true));
            dbCollection.createIndex(
                new BasicDBObject("name", ""),
                new BasicDBObject("name", "name"));
        });
        if (backpackInventory == null) {
            backpackInventory = mongoWrapper.open(
                "survival_slimefun",
                "backpack_inventory"
            );
        }
        backpackInventory.open(dbCollection -> {
            dbCollection.createIndex(
                new BasicDBObject("_id", ""),
                (new BasicDBObject("name", "_id")).append("unique", true));
        });
        if (blockData == null) {
            blockData = mongoWrapper.open(
                "survival_slimefun",
                "block_data"
            );
        }
        blockData.open(dbCollection -> {
            dbCollection.createIndex(
                new BasicDBObject("_id", ""),
                (new BasicDBObject("name", "_id")).append("unique", true));
            dbCollection.createIndex(
                new BasicDBObject("chunk", ""),
                (new BasicDBObject("chunk", "_id")));
        });
        if (chunkData == null) {
            chunkData = mongoWrapper.open(
                "survival_slimefun",
                "chunk_data"
            );
        }
        chunkData.open(dbCollection -> {
            dbCollection.createIndex(
                new BasicDBObject("_id", ""),
                (new BasicDBObject("name", "_id")).append("unique", true));
        });
    }

    private MongoWrapper.MongoDatabaseWrapper mapMongoDB(DataScope scope) {
        return switch (scope) {
            case PLAYER_PROFILE, PLAYER_RESEARCH, BACKPACK_PROFILE -> playerProfile;
            case BACKPACK_INVENTORY -> backpackInventory;
            case BLOCK_INVENTORY, BLOCK_RECORD, BLOCK_DATA -> blockData;
            case CHUNK_DATA -> chunkData;
            case NONE -> throw new IllegalArgumentException("NONE cannot be a storage data scope!");
        };
    }

    @Override
    protected String mapTable(DataScope scope) {
        return switch (scope) {
            case PLAYER_PROFILE -> profileTable;
            case BACKPACK_INVENTORY -> bpInvTable;
            case BACKPACK_PROFILE -> backpackTable;
            case PLAYER_RESEARCH -> researchTable;
            case BLOCK_INVENTORY -> blockInvTable;
            case CHUNK_DATA -> chunkDataTable;
            case BLOCK_DATA -> blockDataTable;
            case BLOCK_RECORD -> blockRecordTable;
            case NONE -> throw new IllegalArgumentException("NONE cannot be a storage data scope!");
        };
    }

    protected String mapKey(DataScope scope) {
        return switch (scope) {
            case PLAYER_PROFILE, PLAYER_RESEARCH, BACKPACK_PROFILE -> "p_uuid";
            case BACKPACK_INVENTORY -> "b_id";
            case BLOCK_INVENTORY, BLOCK_RECORD, BLOCK_DATA -> "loc";
            case CHUNK_DATA -> "chunk";
            case NONE -> throw new IllegalArgumentException("NONE cannot be a storage data scope!");
        };
    }

    protected FieldKey getKeyValue(DataScope scope) {
        return switch (scope) {
            case PLAYER_PROFILE, PLAYER_RESEARCH, BACKPACK_PROFILE -> FieldKey.PLAYER_UUID;
            case BACKPACK_INVENTORY -> FieldKey.BACKPACK_ID;
            case BLOCK_INVENTORY, BLOCK_RECORD, BLOCK_DATA -> FieldKey.LOCATION;
            case CHUNK_DATA -> FieldKey.CHUNK;
            case NONE -> throw new IllegalArgumentException("NONE cannot be a storage data scope!");
        };
    }

    protected HashMap<String, String> mapData(Map<FieldKey, String> data) {
        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<FieldKey, String> entry : data.entrySet()) {
            map.put(SqlUtils.mapField(entry.getKey()), entry.getValue());
        }
        return map;
    }

    @Override
    public void setData(RecordKey key, RecordSet item) {
        Map<FieldKey, String> data = item.getAll();
        Set<FieldKey> updateFields = key.getFields();
        DataScope scope = key.getScope();
        if (!updateFields.isEmpty() && key.getConditions().isEmpty()) {
            throw new IllegalArgumentException("Condition is required for update statement!");
        }
        List<Pair<FieldKey, String>> conditions = key.getConditions();

        if (scope.equals(DataScope.BLOCK_RECORD)) {
            String k = data.get(FieldKey.LOCATION);
            HashMap<String, String> sData = mapData(data);
            sData.remove(SqlUtils.mapField(FieldKey.LOCATION));
            if (conditions.isEmpty()) {
                blockData.open(db -> {
                    db.save(new BasicDBObject(sData).append("_id", k));
                });
            } else {
                blockData.open(db -> {
                    db.update(new BasicDBObject("_id", conditions.get(0).getSecondValue()),
                        new BasicDBObject("$set", new BasicDBObject(sData)),
                        true, false
                    );
                });
            }
        } else if (scope.equals(DataScope.BLOCK_INVENTORY)) {
            String k = data.get(FieldKey.LOCATION);
            blockData.open(db -> {
                db.update(new BasicDBObject("_id", k),
                    new BasicDBObject("$set", new BasicDBObject("inventory." + data.get(FieldKey.INVENTORY_SLOT), data.get(FieldKey.INVENTORY_ITEM))),
                    true, false
                );
            });
        } else if (scope.equals(DataScope.BLOCK_DATA)) {
            //var reqKey = new RecordKey(DataScope.BLOCK_DATA);
            //reqKey.addCondition(FieldKey.LOCATION, blockData.getKey());
            //reqKey.addCondition(FieldKey.DATA_KEY, key);
            String k = data.get(FieldKey.LOCATION);
            blockData.open(db -> {
                db.update(new BasicDBObject("_id", k),
                    new BasicDBObject("$set", new BasicDBObject("data." + data.get(FieldKey.DATA_KEY), data.get(FieldKey.DATA_VALUE))),
                    true, false
                );
            });
        } else if (scope.equals(DataScope.CHUNK_DATA)) {
            String k = data.get(FieldKey.CHUNK);
            chunkData.open(db -> {
                db.update(new BasicDBObject("_id", k),
                    new BasicDBObject("$set", new BasicDBObject("data." + data.get(FieldKey.DATA_KEY), data.get(FieldKey.DATA_VALUE))),
                    true, false
                );
            });
        } else if (scope.equals(DataScope.PLAYER_PROFILE)) {
            String k = data.get(FieldKey.PLAYER_UUID);
            HashMap<String, String> sData = mapData(data);
            sData.remove(SqlUtils.mapField(FieldKey.PLAYER_UUID));
            playerProfile.open(db -> {
                db.update(new BasicDBObject("_id", k),
                    new BasicDBObject("$set", new BasicDBObject(sData)),
                    true, false
                );
            });
        } else if (scope.equals(DataScope.PLAYER_RESEARCH)) {
            String k = data.get(FieldKey.PLAYER_UUID);
            playerProfile.open(db -> {
                db.update(new BasicDBObject("_id", k),
                    new BasicDBObject("$set", new BasicDBObject("research." + data.get(FieldKey.RESEARCH_ID), "")),
                    true, false
                );
            });
        } else if (scope.equals(DataScope.BACKPACK_PROFILE)) {
            String k = data.get(FieldKey.PLAYER_UUID);
            playerProfile.open(db -> {
                db.update(new BasicDBObject("_id", k),
                    new BasicDBObject("$set",
                        new BasicDBObject("backpack." + data.get(FieldKey.BACKPACK_ID),
                            new BasicDBObject("b_num", data.get(FieldKey.BACKPACK_NUMBER))
                                .append("b_name", data.get(FieldKey.BACKPACK_NAME))
                                .append("b_size", data.get(FieldKey.BACKPACK_SIZE))
                        )
                    ),
                    true, false
                );
            });
        } else if (scope.equals(DataScope.BACKPACK_INVENTORY)) {

            String k = data.get(FieldKey.BACKPACK_ID);
            backpackInventory.open(db -> {
                db.update(new BasicDBObject("_id", k),
                    new BasicDBObject("$set", new BasicDBObject(data.get(FieldKey.INVENTORY_SLOT), data.get(FieldKey.INVENTORY_ITEM))),
                    true, false
                );
            });
        }
    }

    @Override
    public List<RecordSet> getData(RecordKey key, boolean distinct) {
        List<Pair<FieldKey, String>> conditions = key.getConditions();

        List<RecordSet> outData = new ArrayList<>();
        DataScope scope = key.getScope();

        if (!conditions.isEmpty()) {
            FieldKey firstValue = conditions.get(0).getFirstValue();
            String secondValue = conditions.get(0).getSecondValue();
            if (firstValue == null) {
                return Collections.emptyList();
            }
            if (secondValue == null) {
                return Collections.emptyList();
            }
            if (scope.equals(DataScope.CHUNK_DATA)) {
                if (firstValue.equals(FieldKey.CHUNK)) {
                    if (distinct) {
                        String replace = secondValue.replace("%", ".*");
                        Pattern compile = Pattern.compile(replace);
                        BasicDBObject id = new BasicDBObject("_id", compile);
                        chunkData.open(db -> {
                            DBCursor dbObjects = db.find(id, new BasicDBObject("_id", 1));
                            for (DBObject dbObject : dbObjects) {
                                RecordSet recordSet = new RecordSet();
                                recordSet.put(FieldKey.CHUNK, dbObject.get("_id").toString());
                                outData.add(recordSet);
                            }
                        });
                    } else {
                        BasicDBObject id = new BasicDBObject("_id", secondValue);
                        HashMap<?, ?> hashMap = chunkData.find(HashMap.class, id);
                        RecordSet recordSet = new RecordSet();
                        if (hashMap == null) {
                            return Collections.emptyList();
                        }
                        hashMap.forEach((k, v) -> {
                            if (!k.equals("_id")) {
                                recordSet.put(FieldKey.DATA_KEY, (String) k);
                                recordSet.put(FieldKey.DATA_VALUE, (String) v);
                            }
                        });
                        outData.add(recordSet);
                    }
                }
            } else if (scope.equals(DataScope.BLOCK_RECORD)) {
                if (firstValue.equals(FieldKey.LOCATION)) {
                    BasicDBObject id = new BasicDBObject("_id", secondValue);
                    HashMap<?, ?> hashMap = blockData.find(HashMap.class, id, new BasicDBObject("sf_id", 1));
                    if (hashMap == null) {
                        return Collections.emptyList();
                    }
                    RecordSet recordSet = new RecordSet();
                    recordSet.put(FieldKey.SLIMEFUN_ID, (String) hashMap.get("sf_id"));
                    outData.add(recordSet);
                } else if (firstValue.equals(FieldKey.CHUNK)) {
                    BasicDBObject id;
                    if (distinct) {
                        String replace = secondValue.replace("%", ".*");
                        Pattern compile = Pattern.compile(replace);
                        id = new BasicDBObject("chunk", compile);
                    } else {
                        id = new BasicDBObject("chunk", secondValue);
                    }

                    blockData.open(db -> {
                        DBCursor dbObjects = db.find(id);
                        for (DBObject dbObject : dbObjects) {
                            RecordSet recordSet = new RecordSet();
                            if (distinct) {
                                recordSet.put(FieldKey.CHUNK, dbObject.get("chunk").toString());
                            } else {
                                for (FieldKey field : key.getFields()) {
                                    if (FieldKey.LOCATION.equals(field)) {
                                        recordSet.put(FieldKey.LOCATION, dbObject.get("_id").toString());
                                    }
                                    if (FieldKey.SLIMEFUN_ID.equals(field)) {
                                        recordSet.put(FieldKey.SLIMEFUN_ID, dbObject.get("sf_id").toString());
                                    }
                                }
                            }
                            outData.add(recordSet);
                        }
                    });
                }
            } else if (scope.equals(DataScope.BLOCK_DATA)) {
                if (firstValue.equals(FieldKey.LOCATION)) {
                    BasicDBObject id = new BasicDBObject("_id", secondValue);
                    HashMap<?, ?> hashMap = blockData.find(HashMap.class, id, new BasicDBObject("data", 1));
                    if (!hashMap.containsKey("data")) {
                        System.out.println("null!");
                        return Collections.emptyList();
                    }
                    ((LinkedTreeMap<?, ?>) hashMap.get("data")).forEach((k, v) -> {
                        RecordSet recordSet = new RecordSet();
                        recordSet.put(FieldKey.DATA_KEY, (String) k);
                        recordSet.put(FieldKey.DATA_VALUE, (String) v);
                        outData.add(recordSet);
                    });
                }
            } else if (scope.equals(DataScope.BLOCK_INVENTORY)) {
                if (firstValue.equals(FieldKey.LOCATION)) {
                    BasicDBObject id = new BasicDBObject("_id", secondValue);
                    HashMap<?, ?> hashMap = blockData.find(HashMap.class, id, new BasicDBObject("inventory", 1));
                    if (hashMap == null) {
                        return Collections.emptyList();
                    }
                    ((LinkedTreeMap<?, ?>) hashMap.get("inventory")).forEach((k, v) -> {
                        RecordSet recordSet = new RecordSet();
                        recordSet.put(FieldKey.INVENTORY_SLOT, (String) k);
                        recordSet.put(FieldKey.INVENTORY_ITEM, (String) v);
                        outData.add(recordSet);
                    });
                }
            } else if (scope.equals(DataScope.PLAYER_PROFILE)) {
                if (firstValue.equals(FieldKey.PLAYER_UUID)) {
                    BasicDBObject id = new BasicDBObject("_id", secondValue);
                    HashMap<?, ?> hashMap = playerProfile.find(HashMap.class, id, new BasicDBObject("b_num", 1));
                    if (hashMap == null) {
                        return Collections.emptyList();
                    }
                    RecordSet recordSet = new RecordSet();
                    recordSet.put(FieldKey.BACKPACK_NUMBER, (String) hashMap.get("b_num"));
                    outData.add(recordSet);
                } else if (firstValue.equals(FieldKey.PLAYER_NAME)) {
                    BasicDBObject id = new BasicDBObject("p_name", secondValue);
                    HashMap<?, ?> hashMap = playerProfile.find(HashMap.class, id, new BasicDBObject("_id", 1));
                    if (hashMap == null) {
                        return Collections.emptyList();
                    }
                    RecordSet recordSet = new RecordSet();
                    recordSet.put(FieldKey.PLAYER_UUID, (String) hashMap.get("_id"));
                    outData.add(recordSet);
                }
            } else if (scope.equals(DataScope.BACKPACK_PROFILE)) {
                if (firstValue.equals(FieldKey.PLAYER_UUID)) {
                    if (conditions.size() == 1) {
                        BasicDBObject id = new BasicDBObject("_id", secondValue);
                        HashMap<?, ?> hashMap = playerProfile.find(HashMap.class, id, new BasicDBObject("backpack", 1));

                        if (hashMap == null) {
                            return Collections.emptyList();
                        }
                        LinkedTreeMap<?, ?> backpack = (LinkedTreeMap<?, ?>) hashMap.get("backpack");
                        for (Object o : backpack.keySet()) {
                            RecordSet recordSet = new RecordSet();
                            recordSet.put(FieldKey.BACKPACK_ID, (String) o);
                            outData.add(recordSet);
                        }
                    } else {
                        BasicDBObject id = new BasicDBObject("_id", secondValue);
                        HashMap<?, ?> hashMap = playerProfile.find(HashMap.class, id, new BasicDBObject("backpack", 1));
                        if (hashMap == null) {
                            return Collections.emptyList();
                        }
                        for (HashMap.Entry<?, ?> entry : ((LinkedTreeMap<?, ?>) hashMap.get("backpack")).entrySet()) {
                            if (entry.getValue() instanceof HashMap<?, ?> map) {
                                if (map.containsKey("b_num")) {
                                    if (((String) map.get("b_num")).equals(conditions.get(1).getSecondValue())) {
                                        RecordSet recordSet = new RecordSet();
                                        recordSet.put(FieldKey.BACKPACK_ID, (String) entry.getKey());
                                        recordSet.put(FieldKey.BACKPACK_SIZE, (String) hashMap.get("b_size"));
                                        recordSet.put(FieldKey.BACKPACK_NAME, (String) hashMap.get("b_name"));
                                        outData.add(recordSet);
                                    }
                                }
                            }
                        }
                    }
                } else if (firstValue.equals(FieldKey.BACKPACK_ID)) {
                    BasicDBObject id = new BasicDBObject("backpack." + secondValue, new BasicDBObject("$exists", true));
                    HashMap<?, ?> hashMap = playerProfile.find(HashMap.class, id, new BasicDBObject("backpack." + secondValue, 1));
                    if (hashMap == null) {
                        return Collections.emptyList();
                    }
                    LinkedTreeMap<?, ?> backpack = (LinkedTreeMap<?, ?>) ((LinkedTreeMap<?, ?>) hashMap.get("backpack")).get(secondValue);
                    RecordSet recordSet = new RecordSet();
                    recordSet.put(FieldKey.BACKPACK_ID, secondValue);
                    recordSet.put(FieldKey.BACKPACK_SIZE, (String) backpack.get("b_size"));
                    recordSet.put(FieldKey.BACKPACK_NAME, (String) backpack.get("b_name"));
                    recordSet.put(FieldKey.BACKPACK_NUMBER, (String) backpack.get("b_num"));
                    recordSet.put(FieldKey.PLAYER_UUID, (String) hashMap.get("_id"));
                    outData.add(recordSet);

                }
            } else if (scope.equals(DataScope.BACKPACK_INVENTORY)) {
                if (firstValue.equals(FieldKey.BACKPACK_ID)) {
                    BasicDBObject id = new BasicDBObject("_id", secondValue);
                    HashMap<?, ?> hashMap = backpackInventory.find(HashMap.class, id);
                    if (hashMap == null) {
                        return Collections.emptyList();
                    }
                    for (Map.Entry<?, ?> entry : hashMap.entrySet()) {
                        if (entry.getKey().equals("_id")) {
                            continue;
                        }
                        RecordSet recordSet = new RecordSet();
                        recordSet.put(FieldKey.INVENTORY_SLOT, (String) entry.getKey());
                        recordSet.put(FieldKey.INVENTORY_ITEM, (String) entry.getValue());
                        outData.add(recordSet);
                    }
                }
            } else if (scope.equals(DataScope.PLAYER_RESEARCH)) {
                if (firstValue.equals(FieldKey.PLAYER_UUID)) {
                    BasicDBObject id = new BasicDBObject("_id", secondValue);
                    HashMap<?, ?> hashMap = playerProfile.find(HashMap.class, id, new BasicDBObject("research", 1));
                    if (hashMap == null) {
                        return Collections.emptyList();
                    }
                    Object researchObj = hashMap.get("research");
                    if (researchObj instanceof LinkedTreeMap<?,?> map) {
                        for (Object research : map.keySet()) {
                            RecordSet recordSet = new RecordSet();
                            recordSet.put(FieldKey.RESEARCH_ID, (String) research);
                            outData.add(recordSet);
                        }
                    }
                }
            }
        }
        if (outData.size() == 0) {
            return Collections.emptyList();
        }


        return outData;
    }

    @Override
    public void deleteData(RecordKey key) {
        List<Pair<FieldKey, String>> conditions = key.getConditions();
        DataScope scope = key.getScope();
        if (!conditions.isEmpty()) {
            FieldKey firstValue = conditions.get(0).getFirstValue();
            String secondValue = conditions.get(0).getSecondValue();

            if (scope.equals(DataScope.BLOCK_RECORD)) {
                if (firstValue.equals(FieldKey.LOCATION)) {
                    blockData.remove(secondValue);
                }
            } else if (scope.equals(DataScope.BLOCK_INVENTORY)) {
                blockData.open(db -> {
                    db.update(new BasicDBObject("_id", secondValue), new BasicDBObject("$unset", new BasicDBObject("inventory." + conditions.get(1).getSecondValue(), "")));
                });
            } else if (scope.equals(DataScope.BLOCK_DATA)) {
                if (conditions.size() == 2) {
                    HashMap hashMap = blockData.find(HashMap.class, new BasicDBObject("_id", secondValue));
                    String sv = conditions.get(1).getSecondValue();
                    if (hashMap != null && hashMap.size() == 2 && sv != null && hashMap.containsKey(sv)) {
                        blockData.remove(secondValue);
                    } else {
                        String prefix;
                        if (conditions.get(1).getFirstValue().equals(FieldKey.DATA_KEY)) {
                            prefix = "data.";
                        } else {
                            prefix = "";
                        }
                        blockData.open(db -> {
                            db.update(new BasicDBObject("_id", secondValue), new BasicDBObject("$unset", new BasicDBObject(prefix + sv, "")));
                        });
                    }
                } else {
                    blockData.remove(secondValue);
                }
            } else if (scope.equals(DataScope.CHUNK_DATA)) {
                if (conditions.size() == 2) {
                    HashMap hashMap = chunkData.find(HashMap.class, new BasicDBObject("_id", secondValue));
                    String sv = conditions.get(1).getSecondValue();
                    if (hashMap != null && hashMap.size() == 2 && sv != null && hashMap.containsKey(sv)) {
                        chunkData.remove(secondValue);
                    } else {
                        chunkData.open(db -> {
                            db.update(new BasicDBObject("_id", secondValue), new BasicDBObject("$unset", new BasicDBObject("data." + sv, "")));
                        });
                    }
                } else {
                    chunkData.remove(secondValue);
                }
            } else if (scope.equals(DataScope.PLAYER_RESEARCH)) {
                playerProfile.open(db -> {
                    db.update(new BasicDBObject("_id", secondValue), new BasicDBObject("$unset", new BasicDBObject("research." + conditions.get(1).getSecondValue(), "")));
                });
            } else if (scope.equals(DataScope.BACKPACK_INVENTORY)) {
                backpackInventory.open(db -> {
                    db.update(new BasicDBObject("_id", secondValue), new BasicDBObject("$unset", new BasicDBObject(conditions.get(1).getSecondValue(), "")));
                });
            }
        }
    }

}
