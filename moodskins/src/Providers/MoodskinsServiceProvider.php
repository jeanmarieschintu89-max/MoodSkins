<?php

namespace Azuriom\Plugin\Moodskins\Providers;

use Azuriom\Extensions\Plugin\BasePluginServiceProvider;
use Azuriom\Plugin\Moodskins\Services\GeyserSkinService;

class MoodskinsServiceProvider extends BasePluginServiceProvider
{
    public function register(): void
    {
        $this->mergeConfigFrom(__DIR__ . '/../../config/moodskins.php', 'moodskins');

        $this->app->singleton(GeyserSkinService::class, function () {
            return new GeyserSkinService();
        });
    }

    public function boot(): void
    {
        $this->loadViews();
        $this->loadTranslations();
        $this->loadRoutesFrom(__DIR__ . '/../../routes/web.php');

        $this->publishes([
            __DIR__ . '/../../config/moodskins.php' => config_path('moodskins.php'),
        ], 'moodskins-config');
    }
}
