<?php

namespace Azuriom\Plugin\Moodskins\Services;

use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Http;

class GeyserSkinService
{
    public function isBedrockName(string $name): bool
    {
        return str_starts_with($name, config('moodskins.bedrock_prefix', '.'));
    }

    public function cleanGamertag(string $name): string
    {
        $prefix = config('moodskins.bedrock_prefix', '.');

        if (str_starts_with($name, $prefix)) {
            return substr($name, strlen($prefix));
        }

        return $name;
    }

    public function getData(string $name): array
    {
        $gamertag = $this->cleanGamertag($name);
        $cacheKey = 'moodskins.bedrock.' . strtolower($gamertag);
        $minutes = (int) config('moodskins.cache_minutes', 1440);

        return Cache::remember($cacheKey, now()->addMinutes($minutes), function () use ($name, $gamertag) {
            $xuid = $this->getXuid($gamertag);
            $skin = $xuid ? $this->getSkin($xuid) : null;

            return [
                'name' => $name,
                'gamertag' => $gamertag,
                'xuid' => $xuid,
                'skin' => $skin,
                'is_bedrock' => $this->isBedrockName($name),
            ];
        });
    }

    public function getXuid(string $gamertag): ?string
    {
        $base = rtrim(config('moodskins.geyser_api_base', 'https://api.geysermc.org/v2'), '/');
        $response = Http::timeout(10)->get($base . '/xbox/xuid/' . rawurlencode($gamertag));

        if (! $response->successful()) {
            return null;
        }

        $json = $response->json();

        if (is_string($json)) {
            return $json;
        }

        return $json['xuid'] ?? $json['id'] ?? null;
    }

    public function getSkin(string $xuid): ?array
    {
        $base = rtrim(config('moodskins.geyser_api_base', 'https://api.geysermc.org/v2'), '/');
        $response = Http::timeout(10)->get($base . '/skin/' . rawurlencode($xuid));

        if (! $response->successful()) {
            return null;
        }

        return $response->json();
    }

    public function getTextureUrl(string $name): ?string
    {
        $data = $this->getData($name);
        $skin = $data['skin'] ?? [];
        $textureId = $skin['texture_id'] ?? $skin['textureId'] ?? null;

        if ($textureId) {
            return 'https://textures.minecraft.net/texture/' . $textureId;
        }

        return $skin['url'] ?? null;
    }
}
