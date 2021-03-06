$input v_positionWorld, v_TBN, v_texcoord0, v_color0
#include "common.sh"



uniform mat4 u_viewProjection;
uniform vec4 u_eyePosition;
uniform mat3 u_highlightPositionLightDirectionLightColor;
uniform vec4 u_numSpecularMipLevelsAnimationTime;


// Seyi NOTE: Not sure if you can pack a mat4 like this, if you can, im really smart
#define u_highlightPosition u_highlightPositionLightDirectionLightColor[0]
#define u_lightDirection u_highlightPositionLightDirectionLightColor[1]
#define u_lightColor u_highlightPositionLightDirectionLightColor[2]
#define u_numSpecularMipLevels u_numSpecularMipLevelsAnimationTime.x
#define u_animationTime u_numSpecularMipLevelsAnimationTime.y
// Seyi NOTE: It seems like the original D3D11 code stored a vec3 in a vec4 , could be wrong
#define u_eyePositionVec3 u_eyePosition.xyz

uniform vec4 u_baseColorFactor;
uniform vec4 u_metallicRoughnessNormalOcclusion;
uniform vec4 u_emissiveAlphaCutoff;

#define u_metallicFactor u_metallicRoughnessNormalOcclusion.x
#define u_roughnessFactor u_metallicRoughnessNormalOcclusion.y
#define u_normalScale u_metallicRoughnessNormalOcclusion.z
#define u_occlusionStrength u_metallicRoughnessNormalOcclusion.w
#define u_emissiveFactor u_emissiveAlphaCutoff.xyz
#define u_alphaCutoff u_emissiveAlphaCutoff.w

SAMPLER2D(u_baseColorTexture, 0);
SAMPLER2D(u_metallicRoughnessTexture, 1);
SAMPLER2D(u_normalTexture, 2);
SAMPLER2D(u_occlusionTexture, 3);
SAMPLER2D(u_emissiveTexture, 4);
SAMPLER2D(u_BRDFTexture, 5);
SAMPLERCUBE(u_specularTexture, 6);
SAMPLERCUBE(u_diffuseTexture, 7);

vec3 f0 = vec3(0.04, 0.04, 0.04);
float MinRoughness = 0.04;
float PI = 3.141592653589793;

vec3 getIBLContribution(float perceptualRoughness, float NdotV, vec3 diffuseColor, vec3 specularColor, vec3 n, vec3 reflection)
{
    float lod = perceptualRoughness * u_numSpecularMipLevels;

    vec3 brdf = texture2D(u_BRDFTexture,vec2(NdotV, 1.0 - perceptualRoughness)).rgb;
    vec3 diffuseLight = textureCube(u_diffuseTexture,n).rgb;
    //DiffuseTexture.Sample(IBLSampler, n).rgb;
    vec3 specularLight = textureCubeLod(u_specularTexture,reflection,lod).rgb;
    //SpecularTexture.SampleLevel(IBLSampler, reflection, lod).rgb;
    vec3 diffuse = diffuseLight * diffuseColor;
    vec3 specular = specularLight * (specularColor * brdf.x + brdf.y);

    return diffuse + specular;
}

vec3 diffuse(vec3 diffuseColor)
{
    return diffuseColor / PI;
}

vec3 specularReflection(vec3 reflectance0, vec3 reflectance90, float VdotH)
{
    return reflectance0 + (reflectance90 - reflectance0) * pow(clamp(1.0 - VdotH, 0.0, 1.0), 5.0);
}

float geometricOcclusion(float NdotL, float NdotV, float alphaRoughness)
{
    float attenuationL = 2.0 * NdotL / (NdotL + sqrt(alphaRoughness * alphaRoughness + (1.0 - alphaRoughness * alphaRoughness) * (NdotL * NdotL)));
    float attenuationV = 2.0 * NdotV / (NdotV + sqrt(alphaRoughness * alphaRoughness + (1.0 - alphaRoughness * alphaRoughness) * (NdotV * NdotV)));
    return attenuationL * attenuationV;
}

float microfacetDistribution(float NdotH, float alphaRoughness)
{
    float roughnessSq = alphaRoughness * alphaRoughness;
    float f = (NdotH * roughnessSq - NdotH) * NdotH + 1.0;
    return roughnessSq / (PI * f * f);
}

void main()
{
    gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0); //vec4(color, baseColor.a);
}
