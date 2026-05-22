<?php

use Azuriom\Plugin\Moodskins\Controllers\MoodSkinsController;
use Illuminate\Support\Facades\Route;

Route::get('/moodskins/ping', function () {
    return response('MoodSkins OK', 200);
})->name('moodskins.ping');

Route::prefix('moodskins')->group(function () {
    Route::get('/data/{name}', [MoodSkinsController::class, 'data'])->name('moodskins.data');
    Route::get('/avatar/{name}', [MoodSkinsController::class, 'avatar'])->name('moodskins.avatar');
    Route::get('/skin/{name}', [MoodSkinsController::class, 'skin'])->name('moodskins.skin');
    Route::get('/head/{name}', [MoodSkinsController::class, 'head'])->name('moodskins.head');
});
