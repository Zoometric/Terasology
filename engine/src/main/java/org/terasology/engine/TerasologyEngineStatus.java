/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine;

/**
 * @author Immortius
 */
public enum TerasologyEngineStatus implements EngineStatus {

    LOADING_CONFIG("Loading config..."),
    PREPPING_SUBSYSTEMS("Preparing Subsystems..."),
    INITIALIZING_ASSET_MANAGEMENT("Initializing Asset Management..."),
    INITIALIZING_SUBSYSTEMS("Initializing Subsystems..."),
    INITIALIZING_MODULE_MANAGER("Initializing Module Management..."),
    INITIALIZING_REFLECTION("Initializing high performance reflection..."),
    INITIALIZING_ASSET_TYPES("Initializing asset types...");

    private final String defaultDescription;

    private TerasologyEngineStatus(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    @Override
    public String getDefaultDescription() {
        return defaultDescription;
    }

    @Override
    public float getProgress() {
        return 0;
    }

    @Override
    public boolean isProgressing() {
        return false;
    }
}
