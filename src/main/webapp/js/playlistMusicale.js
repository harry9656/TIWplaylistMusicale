(function () {
    let pageOrchestrator = new PageOrchestrator();
    let personalMessage, playlistListManagerContainer, songUploaderForm, playlistCarousel, songPlayer;

    window.addEventListener("load", () => {
        if (sessionStorage.getItem("username") == null) {
            window.location.href = "index.html";
        } else {
            pageOrchestrator.start();
            pageOrchestrator.refresh();
        }
    }, false);

    function SongUploaderForm(_songUploaderFormBoxContainer, onResponseMessage) {
        let songUploaderFormBoxContainer = _songUploaderFormBoxContainer;
        let uploadButton = songUploaderFormBoxContainer.querySelector("#uploadNewSong");
        uploadButton.addEventListener("click", function (event) {
            event.preventDefault();
            let form = songUploaderFormBoxContainer.querySelector("form");
            if (form.checkValidity()) {
                makePostCallWithForm("uploadSong", form,
                    () => {
                        form.reset();
                        return onResponseMessage("New song uploaded successfully");
                    },
                    () => onResponseMessage("Unable to uploaded song"));
            } else {
                form.reportValidity();
            }
        })

        this.hide = () => songUploaderFormBoxContainer.style.display = "none";

        this.show = () => {
            songUploaderFormBoxContainer.style.display = "block";
        }
    }

    function PlaylistListManagerContainer(_creatorFormBoxContainer, onResponseMessage) {
        let creatorFormBoxContainer = _creatorFormBoxContainer;
        let resetList = () => {
            let listContainer = creatorFormBoxContainer.querySelector("#listContainer");
            listContainer.innerHTML = "";
            makeCall("GET", "GetPlaylists", response => {
                let playlists = JSON.parse(response);
                playlists.sort((a, b) => {
                    let keyA = new Date(a.creationDate);
                    let keyB = new Date(b.creationDate);
                    if (keyA < keyB) return 1;
                    if (keyA === keyB) return 0;
                    return -1;
                })
                playlists.forEach(playlist => {
                    let li = document.createElement("li");
                    let playlistLink = document.createElement("a");

                    playlistLink.innerText = playlist.title;
                    playlistLink.addEventListener("click", e => {
                        e.preventDefault();
                        playlistListManagerContainer.hide();
                        songUploaderForm.hide();
                        songPlayer.hide();
                        playlistCarousel.show(playlist);
                    })
                    let reorderPage = document.createElement("a");
                    reorderPage.classList.add("hamburger-icon");
                    reorderPage.addEventListener("click", e => {
                        e.preventDefault();
                        window.location.href = "sort.html?playlistId=" + playlist.playlistId + "&playlistTitle=" + playlist.title;
                    })
                    li.appendChild(reorderPage);
                    li.appendChild(playlistLink);
                    listContainer.appendChild(li);
                })
            }, () => onResponseMessage("Unable to fetch playlist data"));
        }
        creatorFormBoxContainer.querySelector("#createNewPlaylistButton")
            .addEventListener("click", (e) => {
                e.preventDefault();
                let form = creatorFormBoxContainer.querySelector("form");
                if (form.checkValidity()) {
                    makePostCallWithForm("CreateEmptyPlaylistPage", form,
                        () => {
                            onResponseMessage("New playlist created");
                            form.reset();
                            resetList()
                        }, () => onResponseMessage("Unable to create new playlist"));
                } else {
                    form.reportValidity();
                }
            })

        this.hide = () => creatorFormBoxContainer.style.display = "none";
        this.show = () => {
            creatorFormBoxContainer.style.display = "block";
            resetList();
        }

    }

    function PlaylistCarousel(_carouselBoxContainer, onResponseMessage) {
        let carouselBoxContainer = _carouselBoxContainer;
        let playlist;
        let songAddingForm = carouselBoxContainer.querySelector("#songAddingForm");
        let addSongToPlaylistButton = carouselBoxContainer.querySelector("#addSongToPlaylist");
        let songsMetaData;
        let offset = 0;
        let nextButton = carouselBoxContainer.querySelector("#nextButton");
        nextButton.addEventListener("click", (nextEvent) => {
            nextEvent.preventDefault();
            offset = offset + 5;
            if (offset >= songsMetaData.length) {
                offset = 0;
            }
            renderSongs();
        })
        let previousButton = carouselBoxContainer.querySelector("#previousButton");
        previousButton.addEventListener("click", (previousEvent) => {
            previousEvent.preventDefault();
            offset = offset - 5;
            if (offset <= 0) {
                offset = 0;
            }
            renderSongs();
        })
        addSongToPlaylistButton.addEventListener("click", (e) => {
            e.preventDefault();
            makePostCallWithForm("AddSongToPlaylist", songAddingForm,
                () => {
                    onResponseMessage("Added song to playlist");
                    offset = 0;
                    renderSongs(true);
                },
                () => onResponseMessage("Unable to add song to playlist"));
        });
        let renderSongs = (reload = false) => {
            if (isNaN(playlist.playlistId)) {
                onResponseMessage("Unable to fetch playlist details");
            } else {
                if (reload) {
                    makeCall("GET", "GetSongsOfPlaylist?playlistId=" + playlist.playlistId, response => {
                        let playlistData = JSON.parse(response);
                        songsMetaData = getSongsSortedByPlaylistData(playlistData.songsMetaDataList, playlistData.playlistSongsList);
                        makeCall("GET", "GetSongsOfUser", songsResponse => {
                            let allSongsMetaData = JSON.parse(songsResponse);
                            let songsThatCanBeAdded = allSongsMetaData.filter(x => !songsMetaData.map(item => item.songId).includes(x.songId))
                            let songToBeAddedSelectComponent = carouselBoxContainer.querySelector("#songToBeAdded");
                            songToBeAddedSelectComponent.innerHTML = "";
                            songsThatCanBeAdded.forEach(song => {
                                let option = document.createElement("option");
                                option.setAttribute("value", song.songId);
                                option.innerText = song.title;
                                songToBeAddedSelectComponent.append(option);
                            });
                            renderSongs();
                        }, () =>
                            onResponseMessage("Unable to load all songs of user"));
                    }, () =>
                        onResponseMessage("Unable to fetch songs of playlist"));
                } else {
                    let tableRowContainer = carouselBoxContainer.querySelector("#songsRowContainer");
                    tableRowContainer.innerHTML = "";
                    if (songsMetaData.length === 0) {
                        let td = document.createElement("td");
                        td.innerText = "This playlist does not contain songs.";
                        tableRowContainer.appendChild(td);
                        previousButton.style.visibility = "hidden";
                        nextButton.style.visibility = "hidden";
                    } else {
                        let songsToBeRendered = songsMetaData.slice(offset, offset + 5);
                        if (offset === 0) {
                            previousButton.style.visibility = "hidden";
                            if (songsMetaData.length - songsToBeRendered.length > 0) {
                                nextButton.style.visibility = "visible";
                            } else {
                                nextButton.style.visibility = "hidden";
                            }
                        } else if (songsMetaData.length - songsToBeRendered.length - offset > 0) {
                            previousButton.style.visibility = "visible";
                            nextButton.style.visibility = "visible";
                        } else {
                            previousButton.style.visibility = "visible";
                            nextButton.style.visibility = "hidden";
                        }

                        songsToBeRendered.forEach(song => {
                            let td = document.createElement("td");
                            let img = document.createElement("img");
                            img.setAttribute("src", "GetSongThumbnail?songThumbnailId=" + song.thumbnailId);
                            img.classList.add("song-thumbnail-small");
                            td.appendChild(img);
                            td.appendChild(document.createElement("br"));
                            let songTitle = document.createElement("a");
                            songTitle.innerText = song.title;
                            songTitle.classList.add("black-link");
                            songTitle.addEventListener("click", (event) => {
                                event.preventDefault();
                                songPlayer.show(song);
                            })
                            td.appendChild(songTitle);
                            tableRowContainer.appendChild(td);
                        })
                    }
                }
            }
            carouselBoxContainer.querySelector("#playlistId").value = playlist.playlistId;
            let playlistTitle = carouselBoxContainer.querySelector("#selectedPlaylistTitle");
            playlistTitle.innerHTML = playlist.title;
        };
        this.hide = () => carouselBoxContainer.style.display = "none";

        this.show = _playlist => {
            playlist = _playlist;
            offset = 0;
            renderSongs(true);
            carouselBoxContainer.style.display = "flex";
        }
    }

    function SongPlayer(_playerBoxContainer) {
        let playerBoxContainer = _playerBoxContainer;
        let songMetaData;
        this.hide = () => {
            playerBoxContainer.style.display = "none";
            playerBoxContainer.innerHTML = '';
        }
        let renderSongPlayer = () => {
            playerBoxContainer.innerHTML = '';
            let thumbnailImg = document.createElement("img");
            thumbnailImg.classList.add("song-thumbnail-big");
            thumbnailImg.setAttribute("src", "GetSongThumbnail?songThumbnailId=" + songMetaData.thumbnailId);
            playerBoxContainer.appendChild(thumbnailImg);
            let titleHeader = document.createElement("h2");
            titleHeader.classList.add("song-title");
            titleHeader.innerText = songMetaData.title;
            playerBoxContainer.appendChild(titleHeader);
            let albumDetail = document.createElement("p");
            albumDetail.classList.add("song-detail");
            albumDetail.innerText = songMetaData.albumTitle;
            playerBoxContainer.appendChild(albumDetail);
            let creditDetail = document.createElement("p");
            creditDetail.classList.add("song-detail");
            creditDetail.innerText = songMetaData.credit;
            playerBoxContainer.appendChild(creditDetail);
            let publicationYearDetail = document.createElement("p");
            publicationYearDetail.classList.add("song-detail");
            publicationYearDetail.innerText = songMetaData.publicationYear.day + "/" + songMetaData.publicationYear.month + "/" + songMetaData.publicationYear.year;
            playerBoxContainer.appendChild(publicationYearDetail);
            let genreDetail = document.createElement("p");
            genreDetail.classList.add("song-detail");
            genreDetail.innerText = songMetaData.genre;
            playerBoxContainer.appendChild(genreDetail);
            let audioPlayer = document.createElement("AUDIO");
            audioPlayer.innerText = "Your browser does not support the audio element.";
            audioPlayer.setAttribute("type", "audio/mpeg");
            audioPlayer.setAttribute("src", "GetSongFile?songFileId=" + songMetaData.songFileId);
            audioPlayer.setAttribute("controls", "controls");
            playerBoxContainer.appendChild(audioPlayer);
        }

        this.show = _songMetaData => {
            playlistListManagerContainer.hide();
            songUploaderForm.hide();
            playlistCarousel.hide();
            songMetaData = _songMetaData;
            renderSongPlayer();
            playerBoxContainer.style.display = "block";
        }

    }

    function PersonalMessage(_username, messageContainer) {
        this.username = _username;
        this.show = function () {
            messageContainer.textContent = "Good to see you again " + this.username;
        }
    }

    function PageOrchestrator() {
        let self = this;
        let homeButton = document.getElementById("homeButton");
        homeButton.addEventListener("click", (event) => {
            event.preventDefault();
            self.refresh()
        });
        this.addNotificationMessage = (message) => {
            let notificationContainer = document.getElementById("notificationMessage");
            notificationContainer.innerText = message;
            notificationContainer.style.display = "block";
            setTimeout(() => {
                notificationContainer.style.display = "none";
                notificationContainer.innerText = "";
            }, 3000)
        }
        this.start = () => {
            personalMessage = new PersonalMessage(sessionStorage.getItem('username'),
                document.getElementById("greetingsTitle"));
            playlistListManagerContainer = new PlaylistListManagerContainer(document.getElementById("playlistCreatorForm"),
                message => self.addNotificationMessage(message));
            songUploaderForm = new SongUploaderForm(document.getElementById("songUploaderFormBoxContainer"),
                message => self.addNotificationMessage(message));
            playlistCarousel = new PlaylistCarousel(document.getElementById("playlistCarouselContainer"),
                message => self.addNotificationMessage(message));
            songPlayer = new SongPlayer(document.getElementById("songPlayerContainer"));
        };
        this.refresh = () => {
            personalMessage.show();
            playlistListManagerContainer.show();
            songUploaderForm.show();
            playlistCarousel.hide();
            songPlayer.hide();
        };
    }
})();
