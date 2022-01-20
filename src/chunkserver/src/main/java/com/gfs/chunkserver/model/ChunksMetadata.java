package com.gfs.chunkserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * created by nikunjagarwal on 16-01-2022
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChunksMetadata {
   private String chunkHandle;
   private String data;
}
