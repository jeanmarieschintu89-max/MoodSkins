<?php

use Illuminate\Support\Facades\Route;

Route::prefix('moodskins')->group(function () {
    Route::get('/data/{name}', 'MoodSkinsController@data')->name('moodskins.data');
    Route::get('/avatar/{name}', 'MoodSkinsController@avatar')->name('moodskins.avatar');
    Route::get('/skin/{name}', 'MoodSkinsController@skin')->name('moodskins.skin');
});
