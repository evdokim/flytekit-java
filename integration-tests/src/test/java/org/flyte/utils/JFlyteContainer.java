/*
 * Copyright 2020 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.flyte.utils;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.shaded.com.google.common.io.Files;

class JFlyteContainer extends GenericContainer<JFlyteContainer> {
  static final String IMAGE_NAME;

  static {
    try {
      File imageNameFile = new File("../jflyte-build/target/docker/image-name");
      IMAGE_NAME = Files.readFirstLine(imageNameFile, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to get image name", e);
    }
  }

  JFlyteContainer(String[] cmd) {
    super(IMAGE_NAME);

    String workingDir = new File("../.").getAbsolutePath();

    withEnv(getFlyteEnv());
    withWorkingDirectory(workingDir);
    withFileSystemBind(workingDir, workingDir, BindMode.READ_ONLY);
    withCommand(cmd);
    waitingFor(new NotRunningWaitStrategy());

    withNetwork(FlyteSandboxNetwork.INSTANCE);

    withLogConsumer(new Slf4jLogConsumer(logger()));
  }

  private Map<String, String> getFlyteEnv() {
    return ImmutableMap.<String, String>builder()
        .put("FLYTE_PLATFORM_URL", "flyte:30081")
        .put("FLYTE_PLATFORM_INSECURE", "True")
        .put("FLYTE_AWS_ENDPOINT", "http://flyte:30084")
        .put("FLYTE_AWS_ACCESS_KEY_ID", "minio")
        .put("FLYTE_AWS_SECRET_ACCESS_KEY", "miniostorage")
        .put("FLYTE_STAGING_LOCATION", "s3://my-s3-bucket")
        .build();
  }
}