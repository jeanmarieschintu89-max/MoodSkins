<?php

namespace Azuriom\Plugin\Moodskins\Providers;

use Azuriom\Extensions\Plugin\BasePluginServiceProvider;
use Illuminate\Support\Facades\Route;

class RouteServiceProvider extends BasePluginServiceProvider
{
    public function boot(): void
    {
        Route::middleware('web')
            ->namespace('Azuriom\\Plugin\\Moodskins\\Controllers')
            ->group(__DIR__ . '/../../routes/web.php');
    }
}
