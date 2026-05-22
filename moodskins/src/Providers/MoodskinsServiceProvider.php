<?php

namespace Azuriom\Plugin\Moodskins\Providers;

use Azuriom\Extensions\Plugin\BasePluginServiceProvider;
use Azuriom\Plugin\Moodskins\Services\GeyserSkinService;
use Illuminate\Support\Facades\Route;

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
        $this->loadMoodSkinsRoutes();

        $this->publishes([
            __DIR__ . '/../../config/moodskins.php' => config_path('moodskins.php'),
        ], 'moodskins-config');
    }

    private function loadMoodSkinsRoutes(): void
    {
        Route::middleware('web')
            ->namespace('Azuriom\\Plugin\\Moodskins\\Controllers')
            ->group(__DIR__ . '/../../routes/web.php');
    }
}
