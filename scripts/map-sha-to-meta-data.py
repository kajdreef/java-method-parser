from pydriller import RepositoryMining, Commit
import sys
import json
from datetime import timezone, timedelta

if __name__ == "__main__":
    project_path = sys.argv[1]

    commit_map = {}

    commit :Commit 
    for commit in RepositoryMining(project_path).traverse_commits():
        sha = commit.hash

        author = commit.author.name

        author_date = commit.author_date
        author_tz =commit.author_timezone
        
        comitter_date = commit.committer_date
        committer_tz =commit.committer_timezone
        
        commit_map[sha] = {
            "author": author,
            "author_date": str(author_date),
            "author_tz": author_tz,
            "comitter_date": str(comitter_date),
            "committer_tz": committer_tz
        }

    
    with open(f'{project_path.split("/")[-1]}.json', 'w') as output_file:
        json.dump(commit_map, output_file)
