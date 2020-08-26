package io.prestosql.tests.product.launcher.cli;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.airlift.airline.Command;
import io.prestosql.tests.product.launcher.Extensions;
import io.prestosql.tests.product.launcher.LauncherModule;
import io.prestosql.tests.product.launcher.suite.SuiteFactory;
import io.prestosql.tests.product.launcher.suite.SuiteModule;

import javax.inject.Inject;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import static io.prestosql.tests.product.launcher.cli.Commands.runCommand;
import static java.util.Objects.requireNonNull;

@Command(name = "list", description = "lists test suites")
public final class SuiteList
        implements Runnable
{
    private final Module additionalSuites;

    public SuiteList(Extensions extensions)
    {
        this.additionalSuites = requireNonNull(extensions, "extensions is null").getAdditionalSuites();
    }

    @Override
    public void run()
    {
        runCommand(
                ImmutableList.<Module>builder()
                        .add(new LauncherModule())
                        .add(new SuiteModule(additionalSuites))
                        .build(),
                SuiteList.Execution.class);
    }

    public static class Execution
            implements Runnable
    {
        private final PrintStream out;
        private final SuiteFactory factory;

        @Inject
        public Execution(SuiteFactory factory)
        {
            this.factory = requireNonNull(factory, "factory is null");

            try {
                this.out = new PrintStream(new FileOutputStream(FileDescriptor.out), true, Charset.defaultCharset().name());
            }
            catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Could not create print stream", e);
            }
        }

        @Override
        public void run()
        {
            out.println("Available suites: ");
            this.factory.listSuites().forEach(out::println);

            out.println("\nAvailable suite configuration profiles: ");
            this.factory.listSuiteConfigs().forEach(out::println);
        }
    }
}