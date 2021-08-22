(function () {

    let pageOrchestrator = new PageOrchestrator();
    let songsListContainer;

    window.addEventListener("load", () => {
        if (sessionStorage.getItem("username") == null) {
            window.location.href = "index.html";
        } else {
            pageOrchestrator.start();
            pageOrchestrator.refresh();
        }
    }, false);

    function SongsListContainer(_listContainer, _playlistId) {
        let listContainer = _listContainer;
        let playlistId = _playlistId;
        let songsMetaData;

        let saveButton = document.getElementById("saveOrder");
        saveButton.addEventListener("click", ev => {
            ev.preventDefault();
            let resultingOrder = [];
            listContainer.childNodes.forEach((value, index) => {
                if (value.nodeName.toUpperCase() === "LI") {
                    let playlistSong = Object.create({});
                    playlistSong.playlistId = playlistId;
                    playlistSong.songId = value.id;
                    playlistSong.orderWeight = index;
                    resultingOrder.push(playlistSong);
                }
            })
            let formData = new FormData();
            formData.append("playlistSongs", JSON.stringify(resultingOrder));
            makePostCallWithForm("SaveOrderOfPlaylist", formData, response => {
                songsListContainer.show();
            }, statusCode => {
                console.log("unable to save");
            })
        })

        function handleDragStart(e) {
            this.classList.add('dragging');
            e.dataTransfer.effectAllowed = 'move';
            e.dataTransfer.setData('text/plain', e.target.id);
        }

        function handleDragEnter(e) {
            this.classList.add('dragover');
        }

        function handleDragLeave(e) {
            this.classList.remove('dragover');
        }

        function handleDragEnd(e) {
            this.classList.remove('dragging');
            if (e.dataTransfer.dropEffect === "none") {
                this.parentNode.appendChild(this);
            }
        }

        function handleDragOver(e) {
            if (e.preventDefault) {
                e.preventDefault();
            }
            e.dataTransfer.dropEffect = 'move';
            return false;
        }

        function handleDrop(e) {
            e.stopPropagation();
            let srcElement = document.getElementById(e.dataTransfer.getData("text/plain"));
            srcElement.classList.remove('dragging');
            if (srcElement !== this) {
                this.classList.remove('dragover');
                this.parentNode.insertBefore(srcElement, this);
            }
            return false;
        }


        this.hide = () => listContainer.style.display = "none";

        function renderSongs() {
            listContainer.innerHTML = "";
            makeCall("GET", "GetSongsOfPlaylist?playlistId=" + playlistId, response => {
                let playlistData = JSON.parse(response);
                songsMetaData = getSongsSortedByPlaylistData(playlistData.songsMetaDataList, playlistData.playlistSongsList);
                songsMetaData.forEach(song => {
                    let li = document.createElement("li");
                    li.setAttribute("id", song.songId);
                    li.setAttribute("draggable", true);
                    li.innerText = song.title;
                    li.addEventListener('dragstart', handleDragStart);
                    li.addEventListener('dragenter', handleDragEnter);
                    li.addEventListener('dragover', handleDragOver);
                    li.addEventListener('dragleave', handleDragLeave);
                    li.addEventListener('drop', handleDrop);
                    li.addEventListener('dragend', handleDragEnd);
                    listContainer.appendChild(li);
                });
            });
        }

        this.show = () => {
            listContainer.style.display = "block";
            renderSongs();
        }
    }

    function PageOrchestrator() {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const params = Object.fromEntries(urlSearchParams.entries());
        let playlistId = params.playlistId;
        document.getElementById("playlistTitle").innerText = params.playlistTitle;
        let homeButton = document.getElementById("homeButton");
        homeButton.addEventListener("click", (event) => {
            event.preventDefault();
            window.location.href = "Home.html";
        });


        this.start = () => {
            songsListContainer = new SongsListContainer(document.getElementById("songListContainer"), playlistId);
        };
        this.refresh = () => {
            songsListContainer.show();
        };
    }
})()