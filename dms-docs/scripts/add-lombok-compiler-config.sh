#!/bin/bash
# Script to add Lombok compiler configuration to service POMs

COMPILER_CONFIG='            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <release>25</release>
                    <proc>full</proc>
                    <compilerArgs>
                        <arg>--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
                        <arg>--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                        <arg>--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
                        <arg>--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED</arg>
                        <arg>--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED</arg>
                        <arg>--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED</arg>
                        <arg>--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
                        <arg>--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED</arg>
                        <arg>--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                        <arg>--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                        <arg>--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                    </compilerArgs>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>1.18.40</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>'

# This script would need to be run manually or we can do it via search_replace
echo "Use search_replace tool to add compiler config to each service POM"
