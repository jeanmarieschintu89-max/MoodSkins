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
        return $this->redirectToTexture($name);
    }

    public function head(string $name): RedirectResponse
    {
        return $this->redirectToTexture($name);
    }

    private function redirectToTexture(string $name): RedirectResponse
    {
        $textureUrl = $this->skins->getTextureUrl($name);

        if ($textureUrl) {
            return redirect()->away($textureUrl);
        }

        return redirect(config('moodskins.fallback_avatar', '/assets/img/avatar.png'));
    }
}
