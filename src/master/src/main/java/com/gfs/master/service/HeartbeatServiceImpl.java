package com.gfs.master.service;

import com.gfs.master.Constants;
import com.gfs.master.model.ChunkServerChunkMetadata;
import com.gfs.master.model.ChunkServerRequest;
import com.gfs.master.model.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * created by nikunjagarwal on 16-01-2022
 */
@Component
@Slf4j
@EnableScheduling
public class HeartbeatServiceImpl {
    static HashMap<String, Date> lastHeartBeatTimeOfServers;

    public HeartbeatServiceImpl(){
        lastHeartBeatTimeOfServers = new HashMap<>();
    }

    /**
     * Scheduled function which iterates over all heartbeats received and
     * removes the dead servers
     */
    @Scheduled(fixedDelay = Constants.heartBeatCheckTimeInterval)
    public static void checkLiveChunkServers() {
        log.info("Checking Live ChunkServers");
        long currentTimeInMillis = new Date().getTime();
        for(Map.Entry lastHeartBeatTime: lastHeartBeatTimeOfServers.entrySet()) {
            String serverUrl = (String) lastHeartBeatTime.getKey();
            Date lastPingTime = (Date) lastHeartBeatTime.getValue();
            long diffInMilliSeconds = currentTimeInMillis - lastPingTime.getTime();
            if(diffInMilliSeconds >= Constants.serverTimeoutTime) {
                log.info("Server {} removed : ", serverUrl);
                lastHeartBeatTimeOfServers.remove(serverUrl);
            } else{
                //TODO : Update chunks metadata and remove this chunkserver
            }
        }
        log.info("Active servers and their last heartbeats: {}", lastHeartBeatTimeOfServers);
    }

    /**
     * Updates the heartbeat of chunkserver with latest heartbeat time
     * @param remoteSocket : url of remote chunkserver
     * @param chunkServerRequest: chunkserver request containing chunk metadata
     */
    public static void updateHeartBeatOfServer(String remoteSocket, ChunkServerRequest chunkServerRequest) {
        log.info("Heartbeat received from {}", remoteSocket);
        lastHeartBeatTimeOfServers.put(remoteSocket, new Date());
        if(chunkServerRequest.isContainsChunksMetadata()){
            ArrayList<ChunkServerChunkMetadata> updatedChunkServerChunkMetadataList = addChunkServerDetails(remoteSocket, chunkServerRequest.getChunkServerChunkMetadataList());
            MetadataServiceImpl metadataService = MetadataServiceImpl.getInstance();
            metadataService.updateChunkMetadata(updatedChunkServerChunkMetadataList);
        }
    }

    /**
     * this method adds the chunkserver url, and the last updated time for chunkserver metadata
     * @param remoteSocket: url of remote chunkserver
     * @param chunkServerChunkMetadataList: list containing chunk metadata
     * @return ArrayList<ChunkServerChunkMetadata>: updated list of chunk metadata
     */
    private static ArrayList<ChunkServerChunkMetadata> addChunkServerDetails(String remoteSocket, ArrayList<ChunkServerChunkMetadata> chunkServerChunkMetadataList){
        ArrayList<ChunkServerChunkMetadata> updatedChunkServerChunkMetadataList = new ArrayList<>();
        for(ChunkServerChunkMetadata chunkServerChunkMetadata: chunkServerChunkMetadataList) {
            Location location = chunkServerChunkMetadata.getLocation();
            location.setChunkserverUrl(remoteSocket);
            location.setLastUpdated(new Date());
            chunkServerChunkMetadata.setLocation(location);
            updatedChunkServerChunkMetadataList.add(chunkServerChunkMetadata);
        }
        return updatedChunkServerChunkMetadataList;
    }
}
