<?php

namespace Azuriom\Plugin\Moodskins\Controllers;

use Azuriom\Http\Controllers\Controller;
use Azuriom\Plugin\Moodskins\Services\GeyserSkinService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\RedirectResponse;

class MoodSkinsController extends Controller
{
    public function __construct(private readonly GeyserSkinService $skins)
    {
    }

    public function data(string $name): JsonResponse
    {
        return response()->json($this->skins->getData($name));
    }

    public function skin(string $name): JsonResponse
    {
        $data = $this->skins->getData($name);

        return response()->json($data['skin'] ?? []);
    }

    public function avatar(string $name): RedirectResponse
    {
        $data = $this->skins->getData($name);
        $fallback = config('moodskins.fallback_avatar', '/assets/img/avatar.png');

        $textureId = $data['skin']['texture_id'] ?? null;

        if ($textureId) {
            return redirect()->away('https://textures.minecraft.net/texture/' . $textureId);
        }

        return redirect($fallback);
    }
}
