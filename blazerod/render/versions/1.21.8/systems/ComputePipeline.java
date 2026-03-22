package top.fifthlight.blazerod.render.version_1_21_8.systems;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ComputePipeline {
    private final ResourceLocation location;
    private final ResourceLocation computeShader;
    private final ShaderDefines shaderDefines;
    private final List<String> samplers;
    private final List<RenderPipeline.UniformDescription> uniforms;
    private final Set<String> storageBuffers;

    protected ComputePipeline(
            ResourceLocation location,
            ResourceLocation computeShader,
            ShaderDefines shaderdefines,
            List<String> samplers,
            List<RenderPipeline.UniformDescription> uniforms,
            Set<String> storageBuffers
    ) {
        this.location = location;
        this.computeShader = computeShader;
        this.shaderDefines = shaderdefines;
        this.samplers = samplers;
        this.uniforms = uniforms;
        this.storageBuffers = storageBuffers;
    }

    public static ComputePipeline.Builder builder(ComputePipeline.Snippet... snippets) {
        var computepipeline$builder = new ComputePipeline.Builder();

        for (var snippet : snippets) {
            computepipeline$builder.withSnippet(snippet);
        }

        return computepipeline$builder;
    }

    @Override
    public String toString() {
        return this.location.toString();
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    public ResourceLocation getComputeShader() {
        return this.computeShader;
    }

    public ShaderDefines getShaderDefines() {
        return this.shaderDefines;
    }

    public List<String> getSamplers() {
        return this.samplers;
    }

    public List<RenderPipeline.UniformDescription> getUniforms() {
        return this.uniforms;
    }

    public Set<String> getStorageBuffers() {
        return this.storageBuffers;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class Builder {
        private Optional<ResourceLocation> location = Optional.empty();
        private Optional<ResourceLocation> computeShader = Optional.empty();
        private Optional<ShaderDefines.Builder> definesBuilder = Optional.empty();
        private Optional<List<String>> samplers = Optional.empty();
        private Optional<List<RenderPipeline.UniformDescription>> uniforms = Optional.empty();
        private Optional<Set<String>> storageBuffers = Optional.empty();

        private Builder() {
        }

        public ComputePipeline.Builder withLocation(ResourceLocation resourceLocation) {
            this.location = Optional.of(resourceLocation);
            return this;
        }

        public ComputePipeline.Builder withComputeShader(ResourceLocation resourceLocation) {
            this.computeShader = Optional.of(resourceLocation);
            return this;
        }

        public ComputePipeline.Builder withShaderDefine(String flag) {
            if (this.definesBuilder.isEmpty()) {
                this.definesBuilder = Optional.of(ShaderDefines.builder());
            }

            this.definesBuilder.get().define(flag);
            return this;
        }

        public ComputePipeline.Builder withShaderDefine(String name, int value) {
            if (this.definesBuilder.isEmpty()) {
                this.definesBuilder = Optional.of(ShaderDefines.builder());
            }

            this.definesBuilder.get().define(name, value);
            return this;
        }

        public ComputePipeline.Builder withShaderDefine(String name, float value) {
            if (this.definesBuilder.isEmpty()) {
                this.definesBuilder = Optional.of(ShaderDefines.builder());
            }

            this.definesBuilder.get().define(name, value);
            return this;
        }

        public ComputePipeline.Builder withSampler(String sampler) {
            if (this.samplers.isEmpty()) {
                this.samplers = Optional.of(new ArrayList<>());
            }

            this.samplers.get().add(sampler);
            return this;
        }

        public ComputePipeline.Builder withUniform(String name, UniformType uniformtype) {
            if (this.uniforms.isEmpty()) {
                this.uniforms = Optional.of(new ArrayList<>());
            }

            if (uniformtype == UniformType.TEXEL_BUFFER) {
                throw new IllegalArgumentException("Cannot use texel buffer without specifying texture format");
            } else {
                this.uniforms.get().add(new RenderPipeline.UniformDescription(name, uniformtype));
                return this;
            }
        }

        public ComputePipeline.Builder withUniform(String name, UniformType uniformtype, TextureFormat textureformat) {
            if (this.uniforms.isEmpty()) {
                this.uniforms = Optional.of(new ArrayList<>());
            }

            if (uniformtype != UniformType.TEXEL_BUFFER) {
                throw new IllegalArgumentException("Only texel buffer can specify texture format");
            } else {
                this.uniforms.get().add(new RenderPipeline.UniformDescription(name, textureformat));
                return this;
            }
        }

        public ComputePipeline.Builder withStorageBuffer(String name) {
            if (this.storageBuffers.isEmpty()) {
                this.storageBuffers = Optional.of(new HashSet<>());
            }

            this.storageBuffers.get().add(name);
            return this;
        }

        void withSnippet(ComputePipeline.Snippet snippet) {
            if (snippet.computeShader.isPresent()) {
                this.computeShader = snippet.computeShader;
            }

            if (snippet.shaderDefines.isPresent()) {
                if (this.definesBuilder.isEmpty()) {
                    this.definesBuilder = Optional.of(ShaderDefines.builder());
                }

                var shaderdefines = snippet.shaderDefines.get();

                for (var entry : shaderdefines.values().entrySet()) {
                    this.definesBuilder.get().define(entry.getKey(), entry.getValue());
                }

                for (var flag : shaderdefines.flags()) {
                    this.definesBuilder.get().define(flag);
                }
            }

            snippet.samplers.ifPresent(list -> {
                if (this.samplers.isPresent()) {
                    this.samplers.get().addAll(list);
                } else {
                    this.samplers = Optional.of(new ArrayList<>(list));
                }
            });
            snippet.uniforms.ifPresent(list -> {
                if (this.uniforms.isPresent()) {
                    this.uniforms.get().addAll(list);
                } else {
                    this.uniforms = Optional.of(new ArrayList<>(list));
                }
            });
            snippet.storageBuffers.ifPresent(list -> {
                if (this.storageBuffers.isPresent()) {
                    this.storageBuffers.get().addAll(list);
                } else {
                    this.storageBuffers = Optional.of(new HashSet<>(list));
                }
            });
        }

        public ComputePipeline.Snippet buildSnippet() {
            return new ComputePipeline.Snippet(
                    this.computeShader,
                    this.definesBuilder.map(ShaderDefines.Builder::build),
                    this.samplers.map(Collections::unmodifiableList),
                    this.uniforms.map(Collections::unmodifiableList),
                    this.storageBuffers.map(Collections::unmodifiableSet)
            );
        }

        public ComputePipeline build() {
            if (this.location.isEmpty()) {
                throw new IllegalStateException("Missing location");
            } else if (this.computeShader.isEmpty()) {
                throw new IllegalStateException("Missing compute shader");
            } else {
                return new ComputePipeline(
                        this.location.get(),
                        this.computeShader.get(),
                        this.definesBuilder.orElse(ShaderDefines.builder()).build(),
                        List.copyOf(this.samplers.orElse(new ArrayList<>())),
                        this.uniforms.orElse(Collections.emptyList()),
                        Set.copyOf(this.storageBuffers.orElse(new HashSet<>()))
                );
            }
        }
    }

    public record Snippet(
            Optional<ResourceLocation> computeShader,
            Optional<ShaderDefines> shaderDefines,
            Optional<List<String>> samplers,
            Optional<List<RenderPipeline.UniformDescription>> uniforms,
            Optional<Set<String>> storageBuffers
    ) {
    }
}
